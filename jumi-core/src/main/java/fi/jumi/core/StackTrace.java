// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Masquerades as another exception instance. Enables transferring exception stack traces between JVMs, without
 * requiring the remote JVM to have every custom exception class on its classpath.
 */
@ThreadSafe
public class StackTrace extends Throwable {

    private final String exceptionClass;
    private final String toString;

    public static StackTrace copyOf(Throwable original) {
        if (original == null) {
            return null;
        }
        return new StackTrace(original);
    }

    private StackTrace(Throwable original) {
        super(original.getMessage(), copyOf(original.getCause()));
        exceptionClass = original.getClass().getName();
        toString = original.toString();
        setStackTrace(original.getStackTrace());
        for (Throwable suppressed : original.getSuppressed()) {
            addSuppressed(copyOf(suppressed));
        }
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        // Our stack trace will be replaced immediately, so no need to generate it
        return this;
    }

    public String getExceptionClass() {
        return exceptionClass;
    }

    @Override
    public String toString() {
        return toString;
    }
}
