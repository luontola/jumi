// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

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
