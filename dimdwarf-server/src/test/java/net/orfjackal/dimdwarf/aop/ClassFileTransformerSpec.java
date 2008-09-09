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

package net.orfjackal.dimdwarf.aop;

import jdave.Group;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.junit.runner.RunWith;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author Esko Luontola
 * @since 5.9.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class ClassFileTransformerSpec extends Specification<Object> {

    private static final String TARGET_CLASS = "net.orfjackal.dimdwarf.aop.ClassFileTransformerSpec$ClassToInstrument";

    private ClassLoader loader;
    private Object target;

    private void initTarget() throws Exception {
        Class<?> cl = loader.loadClass(TARGET_CLASS);
        target = cl.newInstance();
    }


    public class WhenNoTransformerIsInstalled {

        public Object create() throws Exception {
            loader = new TestClassLoader(TARGET_CLASS, null);
            initTarget();
            return null;
        }

        public void classesAreNotInstrumented() {
            specify(target.equals(new Object()), should.equal(false));
        }
    }

    public class WhenATransformerIsInstalled {

        public Object create() throws Exception {
            loader = new TestClassLoader(TARGET_CLASS, new AsmClassFileTransformer() {
                protected ClassVisitor getAdapters(ClassVisitor cv) {
                    return new AddEqualsMethodWhichReturnsTrue(cv);
                }
            });
            initTarget();
            return null;
        }

        public void classesAreInstrumented() {
            specify(target.equals(new Object()), should.equal(true));
        }
    }


    private static class AddEqualsMethodWhichReturnsTrue extends ClassAdapter {

        public AddEqualsMethodWhichReturnsTrue(ClassVisitor cv) {
            super(cv);
        }

        public void visitEnd() {
            MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC, "equals", "(Ljava/lang/Object;)Z", null, null);
            mv.visitCode();
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitMaxs(1, 2);
            mv.visitEnd();
            super.visitEnd();
        }
    }

    public static class ClassToInstrument {
    }
}
