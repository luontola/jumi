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
 * @since 18.8.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class InMemoryDatabaseSpec extends Specification<Object> {

    private static final String TABLE = "test";

    private InMemoryDatabaseManager dbms;
    private TransactionCoordinator tx1;
    private TransactionCoordinator tx2;

    private Blob key = Blob.fromBytes(new byte[]{0});
    private Blob value1 = Blob.fromBytes(new byte[]{1});

    public void create() throws Exception {
        dbms = new InMemoryDatabaseManager();
        Logger txLogger = mock(Logger.class);
        tx1 = new TransactionImpl(txLogger);
        tx2 = new TransactionImpl(txLogger);
        specify(dbms.getOpenConnections(), should.equal(0));
    }


    public class WhenMultipleDatabaseConnectionsAreOpen {

        public void create() {
            dbms.openConnection(tx1.getTransaction()).openTable(TABLE);
            dbms.openConnection(tx2.getTransaction()).openTable(TABLE);
        }

        public void databaseKeepsTrackOfTheNumberOfOpenConnections() {
            specify(dbms.getOpenConnections(), should.equal(2));
            tx1.prepareAndCommit();
            specify(dbms.getOpenConnections(), should.equal(1));
            tx2.prepareAndCommit();
            specify(dbms.getOpenConnections(), should.equal(0));
        }

        public void databaseKeepsTrackOfTheCurrentRevision() {
            specify(dbms.getCurrentRevision(), should.equal(0));
            tx1.prepareAndCommit();
            specify(dbms.getCurrentRevision(), should.equal(1));
            tx2.prepareAndCommit();
            specify(dbms.getCurrentRevision(), should.equal(2));
        }

        public void databaseKeepsTrackOfTheOldestRevisionInUse() {
            specify(dbms.getOldestRevisionInUse(), should.equal(0));
            tx1.prepareAndCommit();
            specify(dbms.getOldestRevisionInUse(), should.equal(0));
            tx2.prepareAndCommit();
            specify(dbms.getOldestRevisionInUse(), should.equal(2));
        }
    }

    public class WhenThereAreDatabaseEntries {

        public void create() {
            DatabaseTable<Blob, Blob> table = dbms.openConnection(tx1.getTransaction()).openTable(TABLE);
            table.update(key, value1);
            tx1.prepareAndCommit();
        }

        public void theyTakeMemorySpace() {
            specify(dbms.getNumberOfKeys(), should.equal(1));
        }

        public void afterDeleteTheMemorySpaceIsFreed() {
            DatabaseTable<Blob, Blob> table = dbms.openConnection(tx2.getTransaction()).openTable(TABLE);
            table.delete(key);
            tx2.prepareAndCommit();

            specify(dbms.getNumberOfKeys(), should.equal(0));
        }
    }
}
