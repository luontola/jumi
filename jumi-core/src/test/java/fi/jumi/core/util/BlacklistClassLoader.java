// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class BlacklistClassLoader extends ClassLoader {
    private final String classNamePrefix;

    public BlacklistClassLoader(String classNamePrefix, ClassLoader parent) {
        super(parent);
        this.classNamePrefix = classNamePrefix;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (name.startsWith(classNamePrefix)) {
            throw new ClassNotFoundException(name);
        }
        return super.loadClass(name);
    }
}
