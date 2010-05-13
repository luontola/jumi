// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.aop;

import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.aop.conf.AbstractTransformationChain;
import org.junit.runner.RunWith;
import org.objectweb.asm.*;

@RunWith(JDaveRunner.class)
@Group({"fast"})
public class ClassFileTransformerSpec extends Specification<Object> {

    private static final String TARGET_CLASS = ClassToInstrument.class.getName();

    private ClassLoader loader;
    private Object target;

    private void initTarget() throws Exception {
        Class<?> cl = loader.loadClass(TARGET_CLASS);
        target = cl.newInstance();
    }


    public class WhenNoTransformerIsInstalled {

        public void create() throws Exception {
            loader = new TransformationTestClassLoader(TARGET_CLASS, null);
            initTarget();
        }

        public void classesAreNotInstrumented() {
            specify(target.equals(new Object()), should.equal(false));
        }
    }

    public class WhenATransformerIsInstalled {

        public void create() throws Exception {
            loader = new TransformationTestClassLoader(TARGET_CLASS, new AbstractTransformationChain() {
                protected ClassVisitor getAdapters(ClassVisitor cv) {
                    return new AddEqualsMethodWhichReturnsTrue(cv);
                }
            });
            initTarget();
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
