// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities.tref;

import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.internal.*;
import net.orfjackal.dimdwarf.db.Blob;
import net.orfjackal.dimdwarf.entities.*;
import net.orfjackal.dimdwarf.serial.*;
import net.orfjackal.dimdwarf.util.StubProvider;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

import java.io.*;
import java.math.BigInteger;
import java.util.*;

/**
 * @author Esko Luontola
 * @since 5.9.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class TransparentReferenceSpec extends Specification<Object> {

    private EntityReferenceFactory referenceFactory;
    private TransparentReferenceFactory proxyFactory;
    private DummyEntity entity;
    private EntityApi entityApi = new DimdwarfEntityApi();

    public void create() throws Exception {
        referenceFactory = mock(EntityReferenceFactory.class);
        proxyFactory = new TransparentReferenceFactoryImpl(StubProvider.wrap(referenceFactory));
        entity = new DummyEntity();
    }

    private Expectations referenceIsCreatedFor(final EntityObject entity, final BigInteger id) {
        return new Expectations() {{
            one(referenceFactory).createReference(entity); will(returnValue(new EntityReferenceImpl<EntityObject>(id, entity)));
        }};
    }


    public class WhenATransparentReferenceProxyIsCreated {

        private Object proxy;

        public void create() {
            checking(referenceIsCreatedFor(entity, BigInteger.ONE));
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
            checking(referenceIsCreatedFor(subclassEntity, BigInteger.TEN));
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
            checking(referenceIsCreatedFor(exceptionThrower, BigInteger.TEN));
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
            ObjectSerializerImpl serializer = new ObjectSerializerImpl(new SerializationListener[0], new SerializationReplacer[]{
                    new ReplaceEntitiesWithTransparentReferences(proxyFactory, entityApi)
            });
            checking(referenceIsCreatedFor(entity, BigInteger.ONE));
            Blob data = serializer.serialize(new SerializationTestEntity(entity, new DummyObject())).getSerializedBytes();
            deserialized = (SerializationTestEntity) serializer.deserialize(data).getDeserializedObject();
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
