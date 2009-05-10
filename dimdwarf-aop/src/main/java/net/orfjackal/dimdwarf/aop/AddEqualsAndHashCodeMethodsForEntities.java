// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.aop;

import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

/**
 * @author Esko Luontola
 * @since 9.9.2008
 */
public class AddEqualsAndHashCodeMethodsForEntities extends ClassAdapter {

    private final String entityAnnotationDesc;
    private final String entityHelperClass;

    private boolean isInterface = false;
    private boolean isEntity = false;
    private boolean hasEqualsMethod = false;
    private boolean hasHashCodeMethod = false;

    public AddEqualsAndHashCodeMethodsForEntities(AopApi api, ClassVisitor cv) {
        super(cv);
        entityAnnotationDesc = "L" + api.getEntityAnnotation() + ";";
        entityHelperClass = api.getEntityHelperClass();
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        isInterface = (access & ACC_INTERFACE) != 0;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (desc.equals(entityAnnotationDesc) && !isInterface) {
            isEntity = true;
        }
        return super.visitAnnotation(desc, visible);
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (name.equals("equals") && desc.equals("(Ljava/lang/Object;)Z")) {
            hasEqualsMethod = true;
        }
        if (name.equals("hashCode") && desc.equals("()I")) {
            hasHashCodeMethod = true;
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    public void visitEnd() {
        if (isEntity) {
            if (!hasEqualsMethod) {
                addEqualsMethod();
            }
            if (!hasHashCodeMethod) {
                addHashCodeMethod();
            }
        }
        super.visitEnd();
    }

    private void addEqualsMethod() {
        MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "equals", "(Ljava/lang/Object;)Z", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESTATIC, entityHelperClass, "equals", "(Ljava/lang/Object;Ljava/lang/Object;)Z");
        mv.visitInsn(IRETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }

    private void addHashCodeMethod() {
        MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "hashCode", "()I", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESTATIC, entityHelperClass, "hashCode", "(Ljava/lang/Object;)I");
        mv.visitInsn(IRETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }
}
