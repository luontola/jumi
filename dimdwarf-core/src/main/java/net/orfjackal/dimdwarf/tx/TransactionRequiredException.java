// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tx;

/**
 * Thrown when a transaction is required but is not active.
 */
public class TransactionRequiredException extends TransactionException {
    private static final long serialVersionUID = 1L;

    public TransactionRequiredException() {
    }

    public TransactionRequiredException(String message) {
        super(message);
    }

    public TransactionRequiredException(Throwable cause) {
        super(cause);
    }

    public TransactionRequiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
