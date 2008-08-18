/*
 * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
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

package net.orfjackal.dimdwarf.db;

import jdave.Group;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.tx.TransactionCoordinator;
import net.orfjackal.dimdwarf.tx.TransactionImpl;
import org.junit.runner.RunWith;

/**
 * @author Esko Luontola
 * @since 18.8.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class ConcurrentDatabaseTransactionsSpec extends Specification<Object> {

    private InMemoryDatabase db;
    private TransactionCoordinator tx1;
    private TransactionCoordinator tx2;
    private Database db1;
    private Database db2;

    private Blob key;
    private Blob value1;
    private Blob value2;

    public void create() throws Exception {
        db = new InMemoryDatabase();
        tx1 = new TransactionImpl();
        tx2 = new TransactionImpl();
        db1 = db.openConnection(tx1.getTransaction());
        db2 = db.openConnection(tx2.getTransaction());

        key = Blob.fromBytes(new byte[]{0});
        value1 = Blob.fromBytes(new byte[]{1});
        value2 = Blob.fromBytes(new byte[]{2});
    }

    public class WhenEntryIsCreatedInATransaction {

        public Object create() {
            db1.update(key, value1);
            return null;
        }

        public void otherTransactionsCanNotSeeIt() {
            specify(db2.read(key), should.equal(null));
        }

        public void afterCommitNewTransactionsCanSeeIt() {
            tx1.prepare();
            tx1.commit();
            Database db3 = db.openConnection(new TransactionImpl().getTransaction());
            specify(db3.read(key), should.equal(value1));
        }
    }
}
