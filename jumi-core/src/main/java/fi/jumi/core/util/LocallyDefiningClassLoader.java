// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import org.apache.commons.io.IOUtils;

import javax.annotation.concurrent.ThreadSafe;
import java.io.*;

@ThreadSafe
public class LocallyDefiningClassLoader extends ClassLoader {
    private final String classNamePrefix;

    public LocallyDefiningClassLoader(String classNamePrefix, ClassLoader parent) {
        super(parent);
        this.classNamePrefix = classNamePrefix;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (name.startsWith(classNamePrefix)) {
            byte[] bytes = getBytecode(name);
            if (bytes != null) {
                defineClass(name, bytes, 0, bytes.length);
            }
        }
        return super.loadClass(name);
    }

    private byte[] getBytecode(String name) {
        String resource = name.replace('.', '/') + ".class";
        InputStream in = getResourceAsStream(resource);
        if (in == null) {
            return null;
        }
        try {
            return IOUtils.toByteArray(in);
        } catch (IOException e) {
            throw Boilerplate.rethrow(e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}
