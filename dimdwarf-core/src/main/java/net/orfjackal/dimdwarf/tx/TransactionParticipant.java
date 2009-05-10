// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tx;

/**
 * @author Esko Luontola
 * @since 17.8.2008
 */
public interface TransactionParticipant {

    /**
     * Called by the transaction coordinator to signify that this participant should prepare for commit.
     * If the participant is unable to prepare (for example because of a transaction conflict), it may
     * throw an exception, in which case the transaction is rolled back.
     */
    void prepare() throws Throwable;

    /**
     * Called by the transaction coordinator to signify that this participant commit. The participant is
     * not allowed to fail the commit, but if it anyways throws an exception, the commit of other participants
     * is not interrupted and the system will possibly end up in an inconsistent state.
     */
    void commit();

    /**
     * Called by the transaction coordinator to signify that this participant rollback. The participant is
     * not allowed to fail the rollback, but if anyways throws an exception, the rollback of other participants
     * is not interrupted and the system will possibly end up in an inconsistent state.
     */
    void rollback();
}
