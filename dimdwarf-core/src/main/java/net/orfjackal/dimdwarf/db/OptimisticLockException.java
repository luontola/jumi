// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

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
