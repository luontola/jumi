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

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;

/**
 * @author Esko Luontola
 * @since 9.9.2008
 */
public class AddEqualsAndHashCodeMethodsForEntities extends ClassAdapter {

    private boolean isEntity = false;
    private boolean hasEqualsMethod = false;
    private boolean hasHashCodeMethod = false;

    public AddEqualsAndHashCodeMethodsForEntities(ClassVisitor cv) {
        super(cv);
    }

    // TODO: would it be best to transform annotated classes only, to avoid the following inheritance issues?

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        for (String s : interfaces) {
            // TODO: will this work for inherited interfaces?
            if (s.equals(DimdwarfApi.ENTITY_INTERFACE)) {
                isEntity = true;
            }
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        // TODO: if equals was defined in "Foo implementes Entity", will "Bar extends Foo" keep the equals from Foo?
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
        mv.visitMethodInsn(INVOKESTATIC, DimdwarfApi.ENTITY_HELPER_CLASS, "equals", "(Ljava/lang/Object;Ljava/lang/Object;)Z");
        mv.visitInsn(IRETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }

    private void addHashCodeMethod() {
        MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "hashCode", "()I", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESTATIC, DimdwarfApi.ENTITY_HELPER_CLASS, "hashCode", "(Ljava/lang/Object;)I");
        mv.visitInsn(IRETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }
}
