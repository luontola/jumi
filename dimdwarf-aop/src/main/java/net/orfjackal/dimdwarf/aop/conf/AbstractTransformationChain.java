/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008-2009, Esko Luontola. All Rights Reserved.
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

package net.orfjackal.dimdwarf.aop.conf;

import org.objectweb.asm.*;

import java.lang.instrument.*;
import java.security.ProtectionDomain;

/**
 * @author Esko Luontola
 * @since 9.9.2008
 */
public abstract class AbstractTransformationChain implements ClassFileTransformer {

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        // TODO: at least the ClassLoader could be passed to the adapters, so they could examine super classes, package annotations etc. 
        ClassReader cr = new ClassReader(classfileBuffer);
        ClassWriter cw;
        if (enableAdditiveTransformationOptimization()) {
            cw = new ClassWriter(cr, 0);
        } else {
            cw = new ClassWriter(0);
        }
        ClassVisitor cv = getAdapters(cw);
        cr.accept(cv, 0);
        return cw.toByteArray();
    }

    /**
     * See "Optimization" in section 2.2.4 of
     * <a href="http://download.forge.objectweb.org/asm/asm-guide.pdf">ASM 3.0 User Guide</a>
     */
    protected boolean enableAdditiveTransformationOptimization() {
        return true;
    }

    protected abstract ClassVisitor getAdapters(ClassVisitor cv);
}
