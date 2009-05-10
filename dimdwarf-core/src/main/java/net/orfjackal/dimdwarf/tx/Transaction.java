// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tx;

/**
 * @author Esko Luontola
 * @since 17.8.2008
 */
public interface Transaction {

    void join(TransactionParticipant p);

    TransactionStatus getStatus();

    boolean isActive();

    void mustBeActive() throws IllegalStateException;

    boolean isRollbackOnly();

    void setRollbackOnly();
}
