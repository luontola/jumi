// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tx;

/**
 * @author Esko Luontola
 * @since 17.8.2008
 */
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
