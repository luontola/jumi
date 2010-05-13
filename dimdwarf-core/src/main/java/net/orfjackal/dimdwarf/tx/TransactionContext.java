// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tx;

import org.slf4j.*;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static net.orfjackal.dimdwarf.tx.TransactionStatus.*;

/**
 * @author Esko Luontola
 * @since 15.8.2008
 */
@ThreadSafe
public class TransactionContext implements Transaction, TransactionCoordinator {
    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(TransactionContext.class);
    private final Logger logger;

    private final Collection<TransactionParticipant> participants = new ConcurrentLinkedQueue<TransactionParticipant>();
    private final Object statusLock = new Object();
    private volatile TransactionStatus status = ACTIVE;
    private volatile boolean rollbackOnly = false;

    // TODO: would it be better to separate TransactionCoordinator and Transaction implementations?

    public TransactionContext() {
        this(DEFAULT_LOGGER);
    }

    public TransactionContext(Logger logger) {
        this.logger = logger;
    }

    public Transaction getTransaction() {
        return this;
    }

    public void join(TransactionParticipant p) {
        mustBeActive();
        if (!participants.contains(p)) {
            participants.add(p);
        }
    }

    public void prepareAndCommit() throws TransactionException {
        prepare();
        commit();
    }

    public void prepare() throws TransactionException {
        changeStatus(ACTIVE, PREPARING);
        try {
            checkIsNotRollbackOnly();
            tryPrepareAllParticipants();
            checkIsNotRollbackOnly();
            changeStatus(PREPARING, PREPARED);
        } catch (Throwable t) {
            changeStatus(PREPARING, PREPARE_FAILED);
            throw new TransactionException("Prepare failed", t);
        }
    }

    public void commit() {
        checkIsNotRollbackOnly();
        changeStatus(PREPARED, COMMITTING);
        commitAllParticipants();
        changeStatus(COMMITTING, COMMITTED);
    }

    public void rollback() {
        TransactionStatus[] from = {ACTIVE, PREPARED, PREPARE_FAILED};
        changeStatus(from, ROLLING_BACK);
        rollbackAllParticipants();
        changeStatus(ROLLING_BACK, ROLLED_BACK);
    }

    private void tryPrepareAllParticipants() throws Throwable {
        for (TransactionParticipant p : participants) {
            p.prepare();
        }
    }

    private void commitAllParticipants() {
        for (TransactionParticipant p : participants) {
            try {
                p.commit();
            } catch (Throwable t) {
                logger.error("Commit failed for participant " + p, t);
            }
        }
    }

    private void rollbackAllParticipants() {
        for (TransactionParticipant p : participants) {
            try {
                p.rollback();
            } catch (Throwable t) {
                logger.error("Rollback failed for participant " + p, t);
            }
        }
    }

    public int getParticipants() {
        return participants.size();
    }

    public TransactionStatus getStatus() {
        return status;
    }

    private void changeStatus(TransactionStatus from, TransactionStatus to) {
        synchronized (statusLock) {
            if (!status.equals(from)) {
                throw new IllegalStateException("Expected " + from + " but was " + status);
            }
            status = to;
        }
    }

    private void changeStatus(TransactionStatus[] fromAny, TransactionStatus to) {
        synchronized (statusLock) {
            for (TransactionStatus from : fromAny) {
                if (status.equals(from)) {
                    status = to;
                    return;
                }
            }
            throw new IllegalStateException("Expected one of " + Arrays.toString(fromAny) + " but was " + status);
        }
    }

    public boolean isActive() {
        return status.equals(ACTIVE);
    }

    public void mustBeActive() throws TransactionRequiredException {
        if (!isActive()) {
            throw new TransactionRequiredException();
        }
    }

    public boolean isRollbackOnly() {
        return rollbackOnly;
    }

    public void setRollbackOnly() {
        rollbackOnly = true;
    }

    private void checkIsNotRollbackOnly() throws TransactionRolledbackException {
        if (rollbackOnly) {
            throw new TransactionRolledbackException("Marked for rollback");
        }
    }
}
