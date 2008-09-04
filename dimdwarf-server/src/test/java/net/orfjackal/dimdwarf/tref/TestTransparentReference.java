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

package net.orfjackal.dimdwarf.tref;

import junit.framework.TestCase;
import net.orfjackal.dimdwarf.api.Entity;
import net.orfjackal.dimdwarf.api.internal.EntityReference;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Esko Luontola
 * @since 25.1.2008
 */
public class TestTransparentReference {

    public static byte[] serializeObject(Object object) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bytes);
        out.writeObject(object);
        out.close();
        return bytes.toByteArray();
    }

    public static Object deserializeObject(byte[] bytes) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Object object = in.readObject();
        in.close();
        return object;
    }

    public static abstract class WhenCreatingATransparentReference extends TestCase {

        protected TransparentReferenceFactory factory;
        private DummyManagedObject object;
        private Object proxy;

        protected void setUp() throws Exception {
            factory = new TransparentReferenceCglibProxyFactory();
//            MockAppContext.install();

            TransparentReferenceImpl.setFactory(factory);
            object = new DummyManagedObject();
            proxy = factory.createTransparentReference(object);
        }

        protected void tearDown() throws Exception {
//            MockAppContext.uninstall();
        }

        public void testAProxyShouldBeCreated() {
            assertNotNull(proxy);
        }

        public void testTheProxyShouldNotBeTheObject() {
            assertNotSame(object, proxy);
        }

        public void testTheProxyShouldImplementTheSameInterfacesAsTheObject() {
            assertTrue(DummyInterface.class.isAssignableFrom(proxy.getClass()));
        }

        public void testTheProxyShouldImplementTheSameInterfacesAsTheObjectsSuperclasses() {
            Object subclassProxy = factory.createTransparentReference(new DummyManagedObject() {
            });
            assertTrue(DummyInterface.class.isAssignableFrom(subclassProxy.getClass()));
        }

        public void testTheProxyShouldNotImplementManagedObject() {
            assertFalse(Entity.class.isAssignableFrom(proxy.getClass()));
        }

        public void testTheProxyShouldBeSerializableAndDeserializable() throws IOException, ClassNotFoundException {
            byte[] bytes = serializeObject(proxy);
            Object deserialized = deserializeObject(bytes);
            assertTrue(deserialized instanceof TransparentReference);
        }

        public void testTheProxyShouldContainAManagedReferenceAndNotAManagedObject() throws IOException {
            final List<Object> objects = new ArrayList<Object>();
            ObjectOutputStream out = new ObjectOutputStream(new ByteArrayOutputStream()) {
                {
                    enableReplaceObject(true);
                }

                protected Object replaceObject(Object obj) throws IOException {
                    objects.add(obj);
                    return obj;
                }
            };
            out.writeObject(proxy);
            out.close();

            boolean containsManagedReference = false;
            for (Object o : objects) {
                assertFalse("Entity instance not allowed: " + o.getClass(), (o instanceof Entity));
                if (o instanceof EntityReference) {
                    containsManagedReference = true;
                }
            }
            assertTrue(containsManagedReference);
        }

        public void testCallingAMethodOnTheProxyShouldCallAMethodOnTheObject() {
            DummyInterface proxy = (DummyInterface) this.proxy;
            assertEquals(1, proxy.dummyMethod());
            assertEquals(1, object.lastValue);
            assertEquals(2, proxy.dummyMethod());
            assertEquals(2, object.lastValue);
        }

        public void testTheProxyShouldShowTheSameExceptionsAsTheObject() {
            DummyManagedObject exceptionThrowing = new DummyManagedObject() {
                public int dummyMethod() {
                    throw new IllegalStateException("foo");
                }
            };
            DummyInterface proxy = (DummyInterface) factory.createTransparentReference(exceptionThrowing);
            try {
                proxy.dummyMethod();
                fail();
            } catch (Exception e) {
                assertEquals(IllegalStateException.class, e.getClass());
                assertEquals("foo", e.getMessage());
            }
        }

        public void testItShouldBePossibleToIdentifyAProxy() {
            assertTrue(proxy instanceof TransparentReference);
            assertFalse(object instanceof TransparentReference);
        }

        public void testItShouldBePossibleToGetTheObjectFromTheProxy() {
            assertSame(object, ((TransparentReference) proxy).getManagedObject());
        }

        public void testItShouldBePossibleToGetTheObjectsTypeFromTheProxy() {
            assertSame(DummyManagedObject.class, ((TransparentReference) proxy).getType());
        }

        public void testItShouldBePossibleToGetTheManagedReferenceFromTheProxy() {
            EntityReference<?> reference = ((TransparentReference) proxy).getManagedReference();
            assertSame(object, reference.get());
        }
    }

    @SuppressWarnings({"FieldCanBeLocal"})
    public static abstract class WhenManagedObjectsAreConvertedToTransparentReferencesDuringSerialization extends TestCase {

        protected TransparentReferenceFactory factory;
        private DummyManagedObject managedObject;
        private DummyInterface normalObject;
        private SerializationTestObject deserialized;

        protected void setUp() throws Exception {
            factory = new TransparentReferenceCglibProxyFactory();
//            MockAppContext.install();

            TransparentReferenceImpl.setFactory(factory);
            managedObject = new DummyManagedObject();
            normalObject = new DummyNormalObject();
            SerializationTestObject object = new SerializationTestObject(managedObject, normalObject);

            ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
            ObjectOutputStream out = new TransparentReferenceConvertingObjectOutputStream(outBytes, factory);
            out.writeObject(object);
            out.close();
            byte[] bytes = outBytes.toByteArray();

            deserialized = (SerializationTestObject) deserializeObject(bytes);
        }

        protected void tearDown() throws Exception {
//            MockAppContext.uninstall();
        }

        public void testShouldReplaceManagedObjectsWithProxies() {
            assertTrue(deserialized.manField instanceof TransparentReference);
        }

        public void testShouldIgnoreNormalObjects() {
            assertEquals(normalObject.getClass(), deserialized.aField.getClass());
        }

        public void testShouldSupportArrays() {
            assertSame(normalObject.getClass(), deserialized.aManArray[0].getClass());
            assertTrue(deserialized.aManArray[1] instanceof TransparentReference);
        }

        private static class SerializationTestObject implements Serializable {
            private static final long serialVersionUID = 1L;

            private DummyInterface aField;
            private DummyInterface manField;
            private DummyInterface[] aManArray;

            public SerializationTestObject(DummyManagedObject managedObject, DummyInterface normalObject) {
                aField = normalObject;
                manField = managedObject;
                aManArray = new DummyInterface[]{normalObject, managedObject};
            }
        }

        private static class DummyNormalObject implements DummyInterface, Serializable {
            private static final long serialVersionUID = 1L;

            public int dummyMethod() {
                return 0;
            }
        }
    }

    public static abstract class MarkingTransparentReferencesForUpdate extends TestCase {

        protected TransparentReferenceFactory factory;
        private DummyManagedObject managedObject;
        private DummyInterface proxy;
        private Object normalObject;

        protected void setUp() throws Exception {
            factory = new TransparentReferenceCglibProxyFactory();
//            MockAppContext.install();

            TransparentReferenceImpl.setFactory(factory);
            managedObject = new DummyManagedObject();
            proxy = (DummyInterface) factory.createTransparentReference(managedObject);
            normalObject = new Object();
        }

        protected void tearDown() throws Exception {
//            MockAppContext.uninstall();
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
}
