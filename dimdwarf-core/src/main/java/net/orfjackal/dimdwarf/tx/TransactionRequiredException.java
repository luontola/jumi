// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tx;

/**
 * Thrown when a transaction is required but is not active.
 *
 * @author Esko Luontola
 * @since 19.8.2008
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
