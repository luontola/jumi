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

import net.orfjackal.dimdwarf.util.ByteUtil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * @author Esko Luontola
 * @since 9.9.2008
 */
public class TestClassLoader extends ClassLoader {

    private final String classToInstrument;
    private final ClassFileTransformer transformer;

    public TestClassLoader(String classToInstrument, ClassFileTransformer transformer) {
        super(TestClassLoader.class.getClassLoader());
        if (transformer == null) {
            transformer = new NullClassFileTransformer();
        }
        this.classToInstrument = classToInstrument;
        this.transformer = transformer;
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (name.equals(classToInstrument)) {
            Class<?> cl = findClass(name);
            if (cl != null) {
                return cl;
            }
        }
        return getParent().loadClass(name);
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            byte[] b = transformer.transform(this, name, null, null, readClassBytes(name));
            return defineClass(name, b, 0, b.length);

        } catch (IllegalClassFormatException e) {
            throw new ClassNotFoundException(name, e);
        }
    }

    private byte[] readClassBytes(String name) throws ClassNotFoundException {
        InputStream in = getParent().getResourceAsStream(name.replaceAll("\\.", "/") + ".class");
        if (in == null) {
            throw new ClassNotFoundException(name);
        }
        try {
            return ByteUtil.asByteArray(in);
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }
    }

    private static class NullClassFileTransformer implements ClassFileTransformer {
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            return classfileBuffer;
        }
    }
}
