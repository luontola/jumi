// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db;

import net.orfjackal.dimdwarf.tx.Retryable;

/**
 * Thrown when an optimistic locking conflict occurs.
 */
public class OptimisticLockException extends PersistenceException implements Retryable {
    private static final long serialVersionUID = 1L;

    public OptimisticLockException() {
    }

    public OptimisticLockException(String message) {
        super(message);
    }

    public OptimisticLockException(Throwable cause) {
        super(cause);
    }

    public OptimisticLockException(String message, Throwable cause) {
        super(message, cause);
    }

    public boolean mayBeRetried() {
        return true;
    }
}
