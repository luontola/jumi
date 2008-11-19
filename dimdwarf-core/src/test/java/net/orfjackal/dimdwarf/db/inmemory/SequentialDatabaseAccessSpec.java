/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
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
 *     * Neither the name of the copyright holder nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
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

import jdave.Block;
import jdave.Group;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.db.Blob;
import static net.orfjackal.dimdwarf.db.Blob.EMPTY_BLOB;
import net.orfjackal.dimdwarf.db.Database;
import net.orfjackal.dimdwarf.db.DatabaseTable;
import net.orfjackal.dimdwarf.tx.TransactionCoordinator;
import net.orfjackal.dimdwarf.tx.TransactionImpl;
import net.orfjackal.dimdwarf.tx.TransactionRequiredException;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

/**
 * @author Esko Luontola
 * @since 18.8.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class SequentialDatabaseAccessSpec extends Specification<Object> {

    private static final String TABLE = "test";

    private InMemoryDatabase dbms;
    private Database<Blob, Blob> db;
    private DatabaseTable<Blob, Blob> table;
    private TransactionCoordinator tx;

    private Blob key;
    private Blob value;
    private Blob otherValue;

    public void create() throws Exception {
        dbms = new InMemoryDatabase();
        tx = new TransactionImpl(mock(Logger.class));
        db = dbms.openConnection(tx.getTransaction());
        table = db.openTable(TABLE);
        key = Blob.fromBytes(new byte[]{1});
        value = Blob.fromBytes(new byte[]{2});
        otherValue = Blob.fromBytes(new byte[]{3});
    }

    private void canNotBeUsed(final Database<Blob, Blob> db, final DatabaseTable<Blob, Blob> table) {
        specify(new Block() {
            public void run() throws Throwable {
                db.openTable(TABLE);
            }
        }, should.raise(TransactionRequiredException.class));
        specify(new Block() {
            public void run() throws Throwable {
                table.read(key);
            }
        }, should.raise(TransactionRequiredException.class));
        specify(new Block() {
            public void run() throws Throwable {
                table.update(key, value);
            }
        }, should.raise(TransactionRequiredException.class));
        specify(new Block() {
            public void run() throws Throwable {
                table.delete(key);
            }
        }, should.raise(TransactionRequiredException.class));
        specify(new Block() {
            public void run() throws Throwable {
                table.firstKey();
            }
        }, should.raise(TransactionRequiredException.class));
        specify(new Block() {
            public void run() throws Throwable {
                table.nextKeyAfter(key);
            }
        }, should.raise(TransactionRequiredException.class));
    }


    public class WhenDatabaseConnectionIsOpened {

        public void theConnectionIsOpen() {
            specify(db, should.not().equal(null));
            specify(table, should.not().equal(null));
            specify(dbms.getOpenConnections(), should.equal(1));
        }

        public void onlyOneConnectionExistsPerTransaction() {
            specify(dbms.openConnection(tx.getTransaction()), should.equal(db));
            specify(dbms.getOpenConnections(), should.equal(1));
        }

        public void connectionCanNotBeUsedAfterPrepare() {
            tx.prepare();
            canNotBeUsed(db, table);
        }

        public void connectionCanNotBeUsedAfterCommit() {
            tx.prepare();
            tx.commit();
            canNotBeUsed(db, table);
        }

        public void connectionCanNotBeUsedAfterRollback() {
            tx.rollback();
            canNotBeUsed(db, table);
        }

        public void connectionCanNotBeUsedAfterPrepareAndRollback() {
            tx.prepare();
            tx.rollback();
            canNotBeUsed(db, table);
        }
    }

    public class WhenEntryDoesNotExist {

        public void itDoesNotExist() {
            specify(table.read(key), should.equal(EMPTY_BLOB));
        }
    }

    public class WhenEntryIsCreated {

        public void create() {
            table.update(key, value);
        }

        public void theValueCanBeRead() {
            specify(table.read(key), should.equal(value));
        }
    }

    public class WhenEntryIsUpdated {

        public void create() {
            table.update(key, value);
            table.update(key, otherValue);
        }

        public void theLatestValueCanBeRead() {
            specify(table.read(key), should.equal(otherValue));
        }
    }

    public class WhenEntryIsDeleted {

        public void create() {
            table.update(key, value);
            table.delete(key);
        }

        public void itDoesNotExistAnymore() {
            specify(table.read(key), should.equal(EMPTY_BLOB));
        }
    }
}
