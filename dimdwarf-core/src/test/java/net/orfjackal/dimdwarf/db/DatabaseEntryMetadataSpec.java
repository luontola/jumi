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

import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.db.inmemory.InMemoryDatabaseManager;
import net.orfjackal.dimdwarf.tx.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

/**
 * @author Esko Luontola
 * @since 2.12.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class DatabaseEntryMetadataSpec extends Specification<Object> {

    private static final String TABLE = "test";

    private DatabaseManager dbms;
    private Logger txLogger;
    private TransactionCoordinator tx;
    private Database<Blob, Blob> db;
    private DatabaseTable<Blob, Blob> realTable;
    private DatabaseTableWithMetadata<Blob, Blob> metaTable;

    private Blob key = Blob.fromBytes(new byte[]{0x10});
    private Blob value1 = Blob.fromBytes(new byte[]{0x11});
    private Blob value2 = Blob.fromBytes(new byte[]{0x12});

    private String propKey = "prop";
    private Blob propValue1 = Blob.fromBytes(new byte[]{0x01});
    private Blob propValue2 = Blob.fromBytes(new byte[]{0x02});

    public void create() throws Exception {
        dbms = new InMemoryDatabaseManager();
        txLogger = mock(Logger.class);
        updateInNewTransaction(TABLE, key, value1);

        tx = new TransactionImpl(txLogger);
        db = dbms.openConnection(tx.getTransaction());
        realTable = db.openTable(TABLE);
        metaTable = new DatabaseTableWithMetadataImpl<Blob, Blob>(db, TABLE);
    }

    private void updateInNewTransaction(String table, Blob key, Blob value) {
        TransactionCoordinator tx = new TransactionImpl(txLogger);
        dbms.openConnection(tx.getTransaction()).openTable(table).update(key, value);
        tx.prepareAndCommit();
    }


    public class WhenAnEntryDoesNotExist {
        private Blob noSuchKey = Blob.fromBytes(new byte[]{0x20});

        public void itsMetadataCanNotBeRead() {
            specify(new Block() {
                public void run() throws Throwable {
                    metaTable.readMetadata(noSuchKey, propKey);
                }
            }, should.raise(IllegalArgumentException.class));
        }

        public void itsMetadataCanNotBeUpdated() {
            specify(new Block() {
                public void run() throws Throwable {
                    metaTable.updateMetadata(noSuchKey, propKey, propValue1);
                }
            }, should.raise(IllegalArgumentException.class));
        }

        public void itsMetadataCanNotBeDeleted() {
            specify(new Block() {
                public void run() throws Throwable {
                    metaTable.deleteMetadata(noSuchKey, propKey);
                }
            }, should.raise(IllegalArgumentException.class));
        }
    }

    public class WhenAnEntryExists {

        public void theEntryMayBeRead() {
            specify(metaTable.read(key), should.equal(value1));
        }

        public void theEntryMayBeUpdated() {
            metaTable.update(key, value2);
            specify(realTable.read(key), should.equal(value2));
        }

        public void theEntryMayBeDeleted() {
            metaTable.delete(key);
            specify(realTable.read(key), should.equal(Blob.EMPTY_BLOB));
        }

        public void tableKeysMayBeIterated() {
            specify(metaTable.firstKey(), should.equal(key));
            specify(metaTable.nextKeyAfter(Blob.EMPTY_BLOB), should.equal(key));
            specify(metaTable.nextKeyAfter(key), should.equal(null));
        }
    }
}
