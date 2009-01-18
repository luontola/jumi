/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008-2009, Esko Luontola. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.orfjackal.dimdwarf.db.inmemory;

import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.db.*;
import net.orfjackal.dimdwarf.tx.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

/**
 * @author Esko Luontola
 * @since 3.12.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class IteratingDatabaseKeysConcurrentlySpec extends Specification<Object> {

    private static final String TABLE = "test";

    private InMemoryDatabaseManager dbms;
    private TransactionCoordinator tx1;
    private TransactionCoordinator tx2;
    private DatabaseTable<Blob, Blob> table1;
    private DatabaseTable<Blob, Blob> table2;
    private Logger txLogger;

    private Blob key1 = Blob.fromBytes(new byte[]{1});
    private Blob key2 = Blob.fromBytes(new byte[]{2});
    private Blob key3 = Blob.fromBytes(new byte[]{3});
    private Blob key4 = Blob.fromBytes(new byte[]{4});
    private Blob value = Blob.fromBytes(new byte[]{9});

    public void create() throws Exception {
        dbms = new InMemoryDatabaseManager();
        txLogger = mock(Logger.class);
        tx1 = new TransactionImpl(txLogger);
        tx2 = new TransactionImpl(txLogger);
    }

    private void updateInNewTransaction(Blob key, Blob value) {
        TransactionCoordinator tx = new TransactionImpl(txLogger);
        dbms.openConnection(tx.getTransaction()).openTable(TABLE).update(key, value);
        tx.prepareAndCommit();
    }


    public class WhenKeysAreCreatedInATransaction {

        public void create() {
            table1 = dbms.openConnection(tx1.getTransaction()).openTable(TABLE);
            table2 = dbms.openConnection(tx2.getTransaction()).openTable(TABLE);
            table1.update(key1, value);
            table1.update(key2, value);
        }

        public void thatTransactionMayIterateTheKeys() {
            specify(table1.firstKey(), should.equal(key1));
            specify(table1.nextKeyAfter(key1), should.equal(key2));
            specify(table1.nextKeyAfter(key2), should.equal(null));
        }

        public void theOtherTransactionCanNotIterateTheKeys() {
            specify(table2.firstKey(), should.equal(null));
            specify(table2.nextKeyAfter(key1), should.equal(null));
            specify(table2.nextKeyAfter(key2), should.equal(null));
        }

        public void evenAfterCommitTheOtherTransactionCanNotIterateTheKeys() {
            tx1.prepareAndCommit();
            theOtherTransactionCanNotIterateTheKeys();
        }
    }

    public class WhenKeysAreDeletedInATransaction {

        public void create() {
            updateInNewTransaction(key1, value);
            updateInNewTransaction(key2, value);
            table1 = dbms.openConnection(tx1.getTransaction()).openTable(TABLE);
            table2 = dbms.openConnection(tx2.getTransaction()).openTable(TABLE);
            table1.delete(key1);
            table1.delete(key2);
        }

        public void thatTransactionCanNotIterateTheKeys() {
            specify(table1.firstKey(), should.equal(null));
            specify(table1.nextKeyAfter(key1), should.equal(null));
            specify(table1.nextKeyAfter(key2), should.equal(null));
        }

        public void theOtherTransactionMayIterateTheKeys() {
            specify(table2.firstKey(), should.equal(key1));
            specify(table2.nextKeyAfter(key1), should.equal(key2));
            specify(table2.nextKeyAfter(key2), should.equal(null));
        }

        public void evenAfterCommitTheOtherTransactionMayIterateTheKeys() {
            tx1.prepareAndCommit();
            theOtherTransactionMayIterateTheKeys();
        }
    }

    public class WhenExistingAndNewlyCreatedKeysAreMixed {

        public void create() {
            updateInNewTransaction(key3, value);
            table1 = dbms.openConnection(tx1.getTransaction()).openTable(TABLE);
        }

        public void existingKeyIsFirstWhenItIsFirst() {
            table1.update(key4, value);
            specify(table1.firstKey(), should.equal(key3));
        }

        public void newlyCreatedKeyIsFirstWhenItIsFirst() {
            table1.update(key2, value);
            specify(table1.firstKey(), should.equal(key2));
        }

        public void existingKeyIsNextWhenItIsNext() {
            table1.update(key4, value);
            specify(table1.nextKeyAfter(key1), should.equal(key3));
        }

        public void newlyCreatedKeyIsNextWhenItIsNext() {
            table1.update(key2, value);
            specify(table1.nextKeyAfter(key1), should.equal(key2));
        }
    }
}
