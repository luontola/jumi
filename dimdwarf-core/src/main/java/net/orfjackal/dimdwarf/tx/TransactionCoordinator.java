// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tx;

public interface TransactionCoordinator {

    Transaction getTransaction();

    void prepareAndCommit() throws TransactionException;

    void prepare() throws TransactionException;

    void commit();

    void rollback();
}
