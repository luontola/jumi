/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 *     * Neither the name of the copyright holder nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.orfjackal.dimdwarf.entities.tref;

import jdave.Block;
import jdave.Group;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.impl.Entities;
import net.orfjackal.dimdwarf.api.impl.EntityReference;
import net.orfjackal.dimdwarf.api.impl.IEntity;
import net.orfjackal.dimdwarf.api.impl.TransparentReference;
import net.orfjackal.dimdwarf.db.Blob;
import net.orfjackal.dimdwarf.entities.*;
import net.orfjackal.dimdwarf.serial.ObjectSerializerImpl;
import net.orfjackal.dimdwarf.serial.SerializationListener;
import net.orfjackal.dimdwarf.serial.SerializationReplacer;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Esko Luontola
 * @since 5.9.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class TransparentReferenceSpec extends Specification<Object> {

    private ReferenceFactory referenceFactory;
    private TransparentReferenceFactory proxyFactory;
    private DummyEntity entity;

    public void create() throws Exception {
        referenceFactory = mock(ReferenceFactory.class);
        proxyFactory = new TransparentReferenceFactoryImpl(referenceFactory);
        entity = new DummyEntity();
    }

    private Expectations referenceIsCreatedFor(final IEntity entity, final BigInteger id) {
        return new Expectations() {{
            one(referenceFactory).createReference(entity); will(returnValue(new EntityReferenceImpl<IEntity>(id, entity)));
        }};
    }


    public class WhenATransparentReferenceProxyIsCreated {

        private Object proxy;

        public Object create() {
            checking(referenceIsCreatedFor(entity, BigInteger.ONE));
            proxy = proxyFactory.createTransparentReference(entity);
            return null;
        }

        public void aProxyIsCreated() {
            specify(proxy, should.not().equal(null));
        }

        public void itIsADifferentObjectThanTheEntity() {
            specify(entity != proxy);
        }

        public void itIsATransparentReference() {
            specify(Entities.isTransparentReference(proxy));
        }

        public void itIsNotAnEntity() {
            specify(Entities.isEntity(proxy), should.equal(false));
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

                protected Object replaceObject(Object obj) throws IOException {
                    serializedObjects.add(obj);
                    return obj;
                }
            };
            out.writeObject(proxy);
            out.close();

            boolean containsEntityReference = false;
            for (Object obj : serializedObjects) {
                specify(Entities.isEntity(obj), should.equal(false));
                if (obj instanceof EntityReference) {
                    containsEntityReference = true;
                }
            }
            specify(containsEntityReference);
        }

        public void proxyMethodsDelegateTheEntityMethods() {
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
            specify(((TransparentReference) proxy).getEntity(), should.equal(entity));
        }

        public void theTypeOfTheEntityCanBeRetrievedFromTheProxy() {
            specify(((TransparentReference) proxy).getType(), should.equal(DummyEntity.class));
        }

        public void theEntityReferenceCanBeRetrievedFromTheProxy() {
            specify(((TransparentReference) proxy).getEntityReference().get(), should.equal(entity));
        }
    }

    public class WhenEntitiesAreSerialized {

        private SerializationTestEntity deserialized;

        public Object create() {
            ObjectSerializerImpl serializer = new ObjectSerializerImpl(new SerializationListener[0], new SerializationReplacer[]{
                    new ReplaceEntitiesWithTransparentReferences(proxyFactory)
            });
            checking(referenceIsCreatedFor(entity, BigInteger.ONE));
            Blob data = serializer.serialize(new SerializationTestEntity(entity, new DummyObject()));
            deserialized = (SerializationTestEntity) serializer.deserialize(data);
            return null;
        }

        public void directlyReferredEntitiesAreReplacedWithTransparentReferences() {
            specify(deserialized.entity instanceof DummyInterface);
            specify(Entities.isTransparentReference(deserialized.entity));
            specify(Entities.isEntity(deserialized.entity), should.equal(false));
        }

        public void theRootEntityIsNotReplaced() {
            specify(deserialized.getClass(), should.equal(SerializationTestEntity.class));
            specify(Entities.isTransparentReference(deserialized), should.equal(false));
            specify(Entities.isEntity(deserialized));
        }

        public void nonEntityObjectsAreNotReplaced() {
            specify(deserialized.normalObject instanceof DummyInterface);
            specify(deserialized.normalObject.getClass(), should.equal(DummyObject.class));
            specify(Entities.isTransparentReference(deserialized.normalObject), should.equal(false));
            specify(Entities.isEntity(deserialized.normalObject), should.equal(false));
        }
    }

/*
    TODO: convert to JDave format (after marking for update is implemented)

    public static class MarkingTransparentReferencesForUpdateTest extends TestCase {

        protected TransparentReferenceFactory factory;
        private DummyManagedObject managedObject;
        private DummyInterface1 proxy;
        private Object normalObject;

        protected void setUp() throws Exception {
            AppContext.setMockDataManager();

            factory = new TransparentReferenceFactoryImpl(AppContext.getDataManager());
            TransparentReferenceFactoryGlobal.setFactory(factory);
            managedObject = new DummyManagedObject();
            proxy = (DummyInterface1) factory.createTransparentReference(managedObject);
            normalObject = new Object();
        }

        protected void tearDown() throws Exception {
            AppContext.setDataManager(null);
        }

        public void testMarkForUpdateOnManagedObjectShouldUseMarkForUpdate() {
            TransparentReferenceUtil.markForUpdate(managedObject);
            // TODO: mock the DataManager
            // unable to test, should call: AppContext.getDataManager().markForUpdate(managedObject);
        }

        public void testMarkForUpdateOnTransparentReferenceShouldUseGetForUpdate() {
            TransparentReferenceUtil.markForUpdate(proxy);
            // TODO: mock the DataManager
            // unable to test, should call: ManagedReference.getForUpdate();
        }

        public void testMarkForUpdateOnNormalObjectShouldDoNothing() {
            TransparentReferenceUtil.markForUpdate(normalObject);
            // TODO: mock the DataManager
            // unable to test, should call nothing
        }
    }
*/


    private static class SerializationTestEntity implements IEntity, Serializable {
        private static final long serialVersionUID = 1L;

        private DummyInterface entity;
        private DummyInterface normalObject;

        public SerializationTestEntity(DummyEntity entity, DummyObject normalObject) {
            this.entity = entity;
            this.normalObject = normalObject;
        }
    }
}
