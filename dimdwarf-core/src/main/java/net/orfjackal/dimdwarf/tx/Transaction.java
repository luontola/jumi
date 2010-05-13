// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tx;

public interface Transaction {

    void join(TransactionParticipant p);

    TransactionStatus getStatus();

    boolean isActive();

    void mustBeActive() throws IllegalStateException;

    boolean isRollbackOnly();

    void setRollbackOnly();
}
