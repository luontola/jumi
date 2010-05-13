// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.aop;

import net.orfjackal.dimdwarf.util.ByteUtil;

import java.io.*;
import java.lang.instrument.*;
import java.security.ProtectionDomain;

public class TransformationTestClassLoader extends ClassLoader {

    private final ClassNameMatcher classesToInstrument;
    private final ClassFileTransformer transformer;

    public TransformationTestClassLoader(String classesToInstrumentPattern, ClassFileTransformer transformer) {
        super(TransformationTestClassLoader.class.getClassLoader());
        if (transformer == null) {
            transformer = new NullClassFileTransformer();
        }
        this.classesToInstrument = new ClassNameMatcher(classesToInstrumentPattern);
        this.transformer = transformer;
    }

    public synchronized Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> c = findLoadedClass(name);
        if (c == null) {
            c = classesToInstrument.matches(name) ? findClass(name) : super.loadClass(name);
        }
        return c;
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
        InputStream in = getResourceAsStream(name.replaceAll("\\.", "/") + ".class");
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
