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

import net.orfjackal.dimdwarf.api.Entity;
import net.orfjackal.dimdwarf.api.internal.TransparentReference;
import net.orfjackal.dimdwarf.entities.DummyInterface;

import java.io.*;

/**
 * Test for whether it is possible to modify the interfaces of serialized classes:
 * <ol>
 * <li>Run step 1 when DummyManagedObject2 <em>does not</em> implement DummyInterface2.</li>
 * <li>Modify DummyManagedObject2 so that it <em>does</em> implement DummyInterface2.</li>
 * <li>Run step 2 and verify that the deserialized class <em>is</em> instanceof DummyInterface2.</li>
 * </ol>
 *
 * @author Esko Luontola
 * @since 26.1.2008
 */
public class ManualTestTransparentReferenceSerialization {

    // TODO: convert to JDave spec (?)

    public static final File FILE = new File(ManualTestTransparentReferenceSerialization.class.getName() + ".ser.tmp");

    public static final TransparentReferenceFactory FACTORY = new TransparentReferenceCglibProxyFactory(AppContext.getDataManager());
//    public static final TransparentReferenceFactory FACTORY = new TransparentReferenceJdkProxyFactory();

    public static class Step1_Serialize {

        public static void main(String[] args) throws IOException {
//            MockAppContext.install();

            TransparentReferenceFactoryGlobal.setFactory(FACTORY);
            DummyInterface proxy = (DummyInterface) FACTORY.createTransparentReference(new DummyManagedObject2());
            System.out.println("proxy = " + proxy);

            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE));
            out.writeObject(proxy);
            out.close();
            System.out.println("Written to " + FILE.getCanonicalPath());

//            MockAppContext.uninstall();
        }
    }

    public static class Step2_Deserialize {
        public static void main(String[] args) throws IOException, ClassNotFoundException {
//            MockAppContext.install();

            TransparentReferenceFactoryGlobal.setFactory(FACTORY);
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILE));
            Object o = in.readObject();
            in.close();

            System.out.println("o.getClass() = " + o.getClass());
            System.out.println("isTransparentReference     = " + (o instanceof TransparentReference)
                    + "\t(expected: true)");
            if (o instanceof TransparentReference) {
                System.out.println("managedObjectFromProxy     = " + ((TransparentReference) o).getEntity()
                        + "\t(expected: null when SGS is mocked)");
            }
            System.out.println("instanceof DummyInterface  = " + (o instanceof DummyInterface)
                    + "\t(expected: " + DummyInterface.class.isAssignableFrom(DummyManagedObject2.class) + ")");
            System.out.println("instanceof DummyInterface2 = " + (o instanceof DummyInterface2)
                    + "\t(expected: " + DummyInterface2.class.isAssignableFrom(DummyManagedObject2.class) + ")");

//            MockAppContext.uninstall();
        }
    }

    public static class Step3_CleanUp {
        public static void main(String[] args) throws IOException {
            FILE.delete();
            System.out.println("Deleted " + FILE.getCanonicalPath());
        }
    }

    public static class DummyManagedObject2 implements
            DummyInterface,
//            DummyInterface2,
            Serializable, Entity {
        private static final long serialVersionUID = 1L;

        public Object getOther() {
            return null;
        }

        public void setOther(Object other) {
        }

        public int dummyMethod2() {
            return 0;
        }

        @SuppressWarnings({"EqualsWhichDoesntCheckParameterClass"})
        public boolean equals(Object obj) {
            return ManagedIdentity.equals(this, obj);
        }

        public int hashCode() {
            return ManagedIdentity.hashCode(this);
        }
    }

    public static interface DummyInterface2 {
        int dummyMethod2();
    }
}
