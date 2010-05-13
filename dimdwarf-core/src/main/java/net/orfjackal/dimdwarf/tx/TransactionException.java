// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tx;

public class TransactionException extends RuntimeException implements Retryable {
    private static final long serialVersionUID = 1L;

    public TransactionException() {
    }

    public TransactionException(String message) {
        super(message);
    }

    public TransactionException(Throwable cause) {
        super(cause);
    }

    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }

    public boolean mayBeRetried() {
        Throwable cause = getCause();
        return cause instanceof Retryable
                && ((Retryable) cause).mayBeRetried();
    }
}
