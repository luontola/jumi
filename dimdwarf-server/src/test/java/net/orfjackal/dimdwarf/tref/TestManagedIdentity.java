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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.orfjackal.dimdwarf.api.Entity;


/**
 * @author Esko Luontola
 * @since 1.2.2008
 */
public class TestManagedIdentity {

    public static Test suite() {
        return new TestSuite(TestManagedIdentity.class.getDeclaredClasses());
    }


    public static class ManagedIdentityContracts extends TestCase {

        protected TransparentReferenceFactory factory;
        private Entity man1;
        private Entity man2;
        private TransparentReference ref1;
        private TransparentReference ref2;
        private Object obj;

        protected void setUp() throws Exception {
            factory = new TransparentReferenceCglibProxyFactory();
//            MockAppContext.install();

            man1 = new DummyManagedObject();
            man2 = new DummyManagedObject();
            ref1 = factory.createTransparentReference(man1);
            ref2 = factory.createTransparentReference(man2);
            obj = new Object();
        }

        protected void tearDown() throws Exception {
//            MockAppContext.uninstall();
        }

        public void testManagedObjectEqualsManagedObject() {
            assertTrue(ManagedIdentity.equals(man1, man1));
            assertFalse(ManagedIdentity.equals(man1, man2));
        }

        public void testTransparentReferenceEqualsTransparentReference() {
            assertTrue(ManagedIdentity.equals(ref1, ref1));
            assertFalse(ManagedIdentity.equals(ref1, ref2));
        }

        public void testManagedObjectEqualsTransparentReference() {
            assertTrue(ManagedIdentity.equals(man1, ref1));
            assertTrue(ManagedIdentity.equals(ref1, man1));
            assertFalse(ManagedIdentity.equals(man1, ref2));
            assertFalse(ManagedIdentity.equals(ref2, man1));
        }

        public void testManagedObjectEqualsNormalObject() {
            assertFalse(ManagedIdentity.equals(man1, obj));
            assertFalse(ManagedIdentity.equals(obj, man1));
        }

        public void testTransparentReferenceEqualsNormalObject() {
            assertFalse(ManagedIdentity.equals(ref1, obj));
            assertFalse(ManagedIdentity.equals(obj, ref1));
        }

        public void testManagedObjectEqualsNull() {
            assertFalse(ManagedIdentity.equals(man1, null));
            assertFalse(ManagedIdentity.equals(null, man1));
        }

        public void testTransparentReferenceEqualsNull() {
            assertFalse(ManagedIdentity.equals(ref1, null));
            assertFalse(ManagedIdentity.equals(null, ref1));
        }

        public void testNormalObjectEqualsNormalObject() {
            assertTrue(ManagedIdentity.equals(obj, obj));
            assertFalse(ManagedIdentity.equals(obj, new Object()));
        }

        public void testNullEqualsNull() {
            assertTrue(ManagedIdentity.equals(null, null));
        }

        public void testDifferenceManagedObjectsHaveDifferenceHashCode() {
            int hc1 = ManagedIdentity.hashCode(man1);
            int hc2 = ManagedIdentity.hashCode(man2);
            assertFalse(hc1 == hc2);
        }

        public void testDifferenceTransparentReferencesHaveDifferenceHashCode() {
            int hc1 = ManagedIdentity.hashCode(ref1);
            int hc2 = ManagedIdentity.hashCode(ref2);
            assertFalse(hc1 == hc2);
        }

        public void testManagedObjectsAndTransparentReferencesHaveTheSameHashCode() {
            assertEquals(ManagedIdentity.hashCode(man1), ManagedIdentity.hashCode(ref1));
            assertEquals(ManagedIdentity.hashCode(man2), ManagedIdentity.hashCode(ref2));
        }

        public void testEqualsOnProxyShouldNotCallManagedObject() {
            Entity man = new FailingManagedObject();
            TransparentReference proxy = factory.createTransparentReference(man);
            try {
                proxy.equals(man);
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }
        }

        public void testHashCodeOnProxyShouldNotCallManagedObject() {
            Entity man = new FailingManagedObject();
            TransparentReference proxy = factory.createTransparentReference(man);
            try {
                proxy.hashCode();
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }
        }

        private class FailingManagedObject extends DummyManagedObject {
            private static final long serialVersionUID = 1L;

            @SuppressWarnings({"EqualsWhichDoesntCheckParameterClass"})
            public boolean equals(Object obj) {
                fail();
                throw new RuntimeException();
            }

            public int hashCode() {
                fail();
                throw new RuntimeException();
            }
        }
    }
}
