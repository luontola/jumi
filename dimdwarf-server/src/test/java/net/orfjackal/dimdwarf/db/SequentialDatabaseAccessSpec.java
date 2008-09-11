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

package net.orfjackal.dimdwarf.db;

import jdave.Block;
import jdave.Group;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import static net.orfjackal.dimdwarf.db.Blob.EMPTY_BLOB;
import net.orfjackal.dimdwarf.tx.TransactionCoordinator;
import net.orfjackal.dimdwarf.tx.TransactionImpl;
import net.orfjackal.dimdwarf.tx.TransactionRequiredException;
import org.junit.runner.RunWith;

/**
 * @author Esko Luontola
 * @since 18.8.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class SequentialDatabaseAccessSpec extends Specification<Object> {

    private static final String TABLE = "test";

    private InMemoryDatabase dbService;
    private DatabaseTable db;
    private TransactionCoordinator tx;

    private Blob key;
    private Blob value;
    private Blob otherValue;

    public void create() throws Exception {
        dbService = new InMemoryDatabase();
        tx = new TransactionImpl();
        db = dbService.openConnection(tx.getTransaction()).table(TABLE);
        key = Blob.fromBytes(new byte[]{1});
        value = Blob.fromBytes(new byte[]{2});
        otherValue = Blob.fromBytes(new byte[]{3});
    }


    public class WhenDatabaseConnectionIsOpened {

        public Object create() {
            return null;
        }

        public void theConnectionIsOpen() {
            specify(db, should.not().equal(null));
            specify(dbService.getOpenConnections(), should.equal(1));
        }

        public void onlyOneConnectionExistsPerTransaction() {
            specify(new Block() {
                public void run() throws Throwable {
                    dbService.openConnection(tx.getTransaction());
                }
            }, should.raise(IllegalArgumentException.class));
            specify(dbService.getOpenConnections(), should.equal(1));
        }

        public void connectionCanNotBeUsedAfterPrepare() {
            tx.prepare();
            canNotBeUsed(db);
        }

        public void connectionCanNotBeUsedAfterCommit() {
            tx.prepare();
            tx.commit();
            canNotBeUsed(db);
        }

        public void connectionCanNotBeUsedAfterRollback() {
            tx.rollback();
            canNotBeUsed(db);
        }

        public void connectionCanNotBeUsedAfterPrepareAndRollback() {
            tx.prepare();
            tx.rollback();
            canNotBeUsed(db);
        }

        private void canNotBeUsed(final DatabaseTable db) {
            specify(new Block() {
                public void run() throws Throwable {
                    db.read(key);
                }
            }, should.raise(TransactionRequiredException.class));
            specify(new Block() {
                public void run() throws Throwable {
                    db.update(key, value);
                }
            }, should.raise(TransactionRequiredException.class));
            specify(new Block() {
                public void run() throws Throwable {
                    db.delete(key);
                }
            }, should.raise(TransactionRequiredException.class));
        }
    }

    public class WhenEntryDoesNotExist {

        public Object create() {
            return null;
        }

        public void itDoesNotExist() {
            specify(db.read(key), should.equal(EMPTY_BLOB));
        }
    }

    public class WhenEntryIsCreated {

        public Object create() {
            db.update(key, value);
            return null;
        }

        public void theValueCanBeRead() {
            specify(db.read(key), should.equal(value));
        }
    }

    public class WhenEntryIsUpdated {

        public Object create() {
            db.update(key, value);
            db.update(key, otherValue);
            return null;
        }

        public void theLatestValueCanBeRead() {
            specify(db.read(key), should.equal(otherValue));
        }
    }

    public class WhenEntryIsDeleted {

        public Object create() {
            db.update(key, value);
            db.delete(key);
            return null;
        }

        public void itDoesNotExistAnymore() {
            specify(db.read(key), should.equal(EMPTY_BLOB));
        }
    }
}
