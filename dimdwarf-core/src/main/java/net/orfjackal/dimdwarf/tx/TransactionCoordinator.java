// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tx;

/**
 * @author Esko Luontola
 * @since 18.8.2008
 */
public interface TransactionCoordinator {

    Transaction getTransaction();

    void prepareAndCommit() throws TransactionException;

    void prepare() throws TransactionException;

    void commit();

    void rollback();
}
