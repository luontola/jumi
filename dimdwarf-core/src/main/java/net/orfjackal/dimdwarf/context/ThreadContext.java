// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.context;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Even though this class is thread-safe, the contexts themselves do not need to be thread-safe,
 * because this class will make sure that they are used in only one thread.
 *
 * @author Esko Luontola
 * @since 5.9.2008
 */
@ThreadSafe
public class ThreadContext {

    private static final ThreadLocal<Context> THREAD_LOCAL = new ThreadLocal<Context>();

    private ThreadContext() {
    }

    public static void runInContext(Context context, Runnable runnable) {
        setUp(context);
        try {
            runnable.run();
        } finally {
            tearDown();
        }
    }

    /**
     * WARNING: Prefer using {@link #runInContext} instead of this method.
     */
    public static void setUp(Context context) {
        if (getCurrentContext() != null) {
            throw new IllegalStateException("Already set up");
        }
        setCurrentContext(context);
    }

    /**
     * WARNING: Prefer using {@link #runInContext} instead of this method.
     */
    public static void tearDown() {
        if (getCurrentContext() == null) {
            throw new IllegalStateException("Already torn down");
        }
        setCurrentContext(null);
    }

    /**
     * WARNING: This method should be used <em>only</em> when it is not
     * possible to access a service through dependency injection.
     * The service locator pattern (which this method uses) makes
     * dependencies non-explicit and code harder to test.
     */
    public static <T> T get(Class<T> service) {
        Context context = getCurrentContext();
        if (context == null) {
            throw new IllegalStateException("Not set up");
        }
        return context.get(service);
    }

    /**
     * WARNING: This method should be used <em>only</em> if the framework
     * requires direct access to the context implementation.
     */
    @Nullable
    public static Context getCurrentContext() {
        return THREAD_LOCAL.get();
    }

    private static void setCurrentContext(@Nullable Context context) {
        THREAD_LOCAL.set(context);
    }
}
