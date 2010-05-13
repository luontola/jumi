// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tx;

/**
 * Indicates that the transaction has been rolled back, or marked to roll back.
 */
public class TransactionRolledbackException extends TransactionException {
    private static final long serialVersionUID = 1L;

    public TransactionRolledbackException() {
    }

    public TransactionRolledbackException(String message) {
        super(message);
    }

    public TransactionRolledbackException(Throwable cause) {
        super(cause);
    }

    public TransactionRolledbackException(String message, Throwable cause) {
        super(message, cause);
    }
}
