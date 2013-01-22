// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.ThreadFactory;

@ThreadSafe
public class ContextClassLoaderThreadFactory implements ThreadFactory {

    private final ClassLoader contextClassLoader;
    private final ThreadFactory delegate;

    public ContextClassLoaderThreadFactory(ClassLoader contextClassLoader, ThreadFactory delegate) {
        this.contextClassLoader = contextClassLoader;
        this.delegate = delegate;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = delegate.newThread(r);
        thread.setContextClassLoader(contextClassLoader);
        return thread;
    }
}
