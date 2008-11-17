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

import jdave.Group;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.db.Blob;
import net.orfjackal.dimdwarf.db.DatabaseManager;
import net.orfjackal.dimdwarf.db.DatabaseTable;
import net.orfjackal.dimdwarf.tx.TransactionCoordinator;
import net.orfjackal.dimdwarf.tx.TransactionImpl;
import org.junit.runner.RunWith;

/**
 * @author Esko Luontola
 * @since 12.9.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class IteratingDatabaseKeysSpec extends Specification<Object> {

    private static final String TABLE = "test";

    private TransactionCoordinator tx;
    private DatabaseManager dbms;
    private DatabaseTable<Blob, Blob> table;

    private Blob key1;
    private Blob key2;
    private Blob key3;
    private Blob key4;
    private Blob value;

    public void create() throws Exception {
        dbms = new InMemoryDatabase();
        beginNewTransaction();

        key1 = Blob.fromBytes(new byte[]{1});
        key2 = Blob.fromBytes(new byte[]{2});
        key3 = Blob.fromBytes(new byte[]{3});
        key4 = Blob.fromBytes(new byte[]{4});
        value = Blob.fromBytes(new byte[]{42});
    }

    private void beginNewTransaction() {
        tx = new TransactionImpl();
        table = dbms.openConnection(tx.getTransaction()).openTable(TABLE);
    }


    public class AnEmptyDatabaseTable {

        public Object create() {
            return null;
        }

        public void firstKey() {
            specify(table.firstKey(), should.equal(null));
        }

        public void nextKeyAfterNonexistantKey() {
            specify(table.nextKeyAfter(key1), should.equal(null));
        }
    }

    public class ANonEmptyDatabaseTable {

        public Object create() {
            table.update(key1, value);
            table.update(key3, value);
            tx.prepareAndCommit();
            beginNewTransaction();
            return null;
        }

        public void firstKey() {
            specify(table.firstKey(), should.equal(key1));
        }

        public void nextKeyAfterExistingKey() {
            specify(table.nextKeyAfter(key1), should.equal(key3));
        }

        public void nextKeyAfterNonexitentKey() {
            specify(table.nextKeyAfter(key2), should.equal(key3));
        }

        public void nextKeyAfterExistingLastKey() {
            specify(table.nextKeyAfter(key3), should.equal(null));
        }

        public void nextKeyAfterNonexistentLastKey() {
            specify(table.nextKeyAfter(key4), should.equal(null));
        }
    }
}
