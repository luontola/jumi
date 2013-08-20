// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.api;

import javax.annotation.concurrent.*;

/**
 * Masquerades as another exception instance. Enables transferring exception stack traces between JVMs, without
 * requiring the remote JVM to have every custom exception class on its classpath.
 */
@ThreadSafe
public class StackTrace extends Throwable {

    private final String exceptionClass;
    private final String toString;

    public static StackTrace from(Throwable original) {
        if (original == null) {
            return null;
        }
        if (original instanceof StackTrace) {
            return (StackTrace) original;
        }
        return new Builder()
                .setExceptionClass(original.getClass().getName())
                .setToString(original.toString())
                .setMessage(original.getMessage())
                .setStackTrace(original.getStackTrace())
                .setCause(original.getCause())
                .setSuppressed(original.getSuppressed())
                .build();
    }

    private StackTrace(String exceptionClass, String toString, String message, StackTrace cause) {
        super(message, cause);
        this.exceptionClass = exceptionClass;
        this.toString = toString;
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


    @NotThreadSafe
    public static class Builder {
        private String exceptionClass;
        private String toString;
        private String message;
        private StackTraceElement[] stackTrace;
        private Throwable cause;
        private Throwable[] suppressed;

        public Builder setExceptionClass(String exceptionClass) {
            this.exceptionClass = exceptionClass;
            return this;
        }

        public Builder setToString(String toString) {
            this.toString = toString;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setStackTrace(StackTraceElement[] stackTrace) {
            this.stackTrace = stackTrace;
            return this;
        }

        public Builder setCause(Throwable cause) {
            this.cause = cause;
            return this;
        }

        public Builder setSuppressed(Throwable[] suppressed) {
            this.suppressed = suppressed;
            return this;
        }

        public StackTrace build() {
            StackTrace st = new StackTrace(exceptionClass, toString, message, StackTrace.from(cause));
            st.setStackTrace(stackTrace);
            for (Throwable t : suppressed) {
                st.addSuppressed(StackTrace.from(t));
            }
            return st;
        }
    }
}
