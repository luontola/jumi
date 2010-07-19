// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities.tref;

import com.google.inject.Injector;
import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.api.internal.*;
import net.orfjackal.dimdwarf.db.Blob;
import net.orfjackal.dimdwarf.entities.*;
import net.orfjackal.dimdwarf.serial.*;
import net.orfjackal.dimdwarf.util.StubProvider;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

import java.io.*;
import java.util.*;

@RunWith(JDaveRunner.class)
@Group({"fast"})
public class TransparentReferenceSpec extends Specification<Object> {

    private static final EntityId ID1 = new EntityObjectId(1);
    private static final EntityId ID2 = new EntityObjectId(2);

    private EntityReferenceFactory referenceFactory;
    private TransparentReferenceFactory proxyFactory;
    private DummyEntity entity;
    private EntityApi entityApi = new DimdwarfEntityApi();

    public void create() throws Exception {
        referenceFactory = mock(EntityReferenceFactory.class);
        proxyFactory = new TransparentReferenceFactory(StubProvider.wrap(referenceFactory));
        entity = new DummyEntity();
    }

    private Expectations referenceIsCreatedFor(final EntityObject entity, final EntityId id) {
        return new Expectations() {{
            one(referenceFactory).createReference(entity); will(returnValue(new EntityReferenceImpl<EntityObject>(id, entity)));
        }};
    }


    public class WhenATransparentReferenceProxyIsCreated {

        private Object proxy;

        public void create() {
            checking(referenceIsCreatedFor(entity, ID1));
            proxy = proxyFactory.createTransparentReference(entity);
        }

        public void aProxyIsCreated() {
            specify(proxy, should.not().equal(null));
        }

        public void itIsADifferentObjectThanTheEntity() {
            specify(entity != proxy);
        }

        public void itIsATransparentReference() {
            specify(entityApi.isTransparentReference(proxy));
        }

        public void itIsNotAnEntity() {
            specify(entityApi.isEntity(proxy), should.equal(false));
        }

        public void itImplementsTheSameInterfacesAsTheEntity() {
            specify(proxy instanceof DummyInterface);
        }

        public void itImplementsTheSameInterfacesAsTheSuperclassesOfTheEntity() {
            final DummyEntity subclassEntity = new DummyEntity() {
            };
            checking(referenceIsCreatedFor(subclassEntity, ID2));
            TransparentReference subclassProxy = proxyFactory.createTransparentReference(subclassEntity);
            specify(subclassProxy instanceof DummyInterface);
        }

        public void byDefaultItDoesNotExtendTheEntity() {
            specify(proxy instanceof DummyEntity, should.equal(false));
        }

        public void itRefersToTheEntityThroughtAnEntityReference() throws IOException {
            final List<Object> serializedObjects = new ArrayList<Object>();
            ObjectOutputStream out = new ObjectOutputStream(new ByteArrayOutputStream()) {
                {
                    enableReplaceObject(true);
                }

                protected Object replaceObject(Object obj) {
                    serializedObjects.add(obj);
                    return obj;
                }
            };
            out.writeObject(proxy);
            out.close();

            boolean containsEntityReference = false;
            for (Object obj : serializedObjects) {
                specify(entityApi.isEntity(obj), should.equal(false));
                if (obj instanceof EntityReference) {
                    containsEntityReference = true;
                }
            }
            specify(containsEntityReference);
        }

        public void proxyMethodsDelegateToTheEntityMethods() {
            DummyInterface proxy = (DummyInterface) this.proxy;
            proxy.setOther("set through proxy");
            specify(entity.other, should.equal("set through proxy"));
            entity.other = "set directly";
            specify(proxy.getOther(), should.equal("set directly"));
        }

        public void proxyMethodsRethrowExceptionsThrownByTheEntityMethods() {
            DummyEntity exceptionThrower = new DummyEntity() {
                public Object getOther() {
                    throw new IllegalArgumentException("thrown by entity");
                }
            };
            checking(referenceIsCreatedFor(exceptionThrower, ID2));
            final DummyInterface proxy = (DummyInterface) proxyFactory.createTransparentReference(exceptionThrower);
            specify(new Block() {
                public void run() throws Throwable {
                    proxy.getOther();
                }
            }, should.raise(IllegalArgumentException.class, "thrown by entity"));
        }

        public void theEntityCanBeRetrievedFromTheProxy() {
            specify(((TransparentReference) proxy).getEntity$TREF(), should.equal(entity));
        }

        public void theTypeOfTheEntityCanBeRetrievedFromTheProxy() {
            specify(((TransparentReference) proxy).getType$TREF(), should.equal(DummyEntity.class));
        }

        public void theEntityReferenceCanBeRetrievedFromTheProxy() {
            specify(((TransparentReference) proxy).getEntityReference$TREF().get(), should.equal(entity));
        }
    }

    public class WhenEntitiesAreSerialized {

        private SerializationTestEntity deserialized;

        public void create() {
            final Injector injector = mock(Injector.class);
            SerializationFilter filter = new TrefAwareEntitySerializationFilter(
                    new TransparentReferenceSerializationSupport(proxyFactory, entityApi),
                    new SerializationAllowedPolicy(entityApi),
                    injector
            );
            checking(new Expectations() {{
                allowing(injector).injectMembers(with(any(Object.class)));
            }});
            ObjectSerializer serializer = new ObjectSerializer();

            checking(referenceIsCreatedFor(entity, ID1));
            SerializationTestEntity original = new SerializationTestEntity(entity, new DummyObject());
            Blob data = serializer.serialize(original, filter);
            deserialized = (SerializationTestEntity) serializer.deserialize(data, filter);
        }

        public void directlyReferredEntitiesAreReplacedWithTransparentReferences() {
            specify(deserialized.entity instanceof DummyInterface);
            specify(entityApi.isTransparentReference(deserialized.entity));
            specify(entityApi.isEntity(deserialized.entity), should.equal(false));
        }

        public void theRootEntityIsNotReplaced() {
            specify(deserialized.getClass(), should.equal(SerializationTestEntity.class));
            specify(entityApi.isTransparentReference(deserialized), should.equal(false));
            specify(entityApi.isEntity(deserialized));
        }

        public void nonEntityObjectsAreNotReplaced() {
            specify(deserialized.normalObject instanceof DummyInterface);
            specify(deserialized.normalObject.getClass(), should.equal(DummyObject.class));
            specify(entityApi.isTransparentReference(deserialized.normalObject), should.equal(false));
            specify(entityApi.isEntity(deserialized.normalObject), should.equal(false));
        }
    }


    private static class SerializationTestEntity implements EntityObject, Serializable {
        private static final long serialVersionUID = 1L;

        private DummyInterface entity;
        private DummyInterface normalObject;

        public SerializationTestEntity(DummyEntity entity, DummyObject normalObject) {
            this.entity = entity;
            this.normalObject = normalObject;
        }
    }
}
