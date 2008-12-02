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
    private static final String PROPERTY = "prop";

    private DatabaseManager dbms;
    private Database<Blob, Blob> db;
    private DatabaseTableWithMetadata<Blob, Blob> table;
    private TransactionCoordinator tx;

    private Blob key;
    private Blob value;
    private Blob otherValue;

    public void create() throws Exception {
        dbms = new InMemoryDatabaseManager();
        tx = new TransactionImpl(mock(Logger.class));
        db = dbms.openConnection(tx.getTransaction());
        table = new DatabaseTableWithMetadataImpl<Blob, Blob>(dbms, tx.getTransaction());

        key = Blob.fromBytes(new byte[]{1});
        value = Blob.fromBytes(new byte[]{2});
        otherValue = Blob.fromBytes(new byte[]{3});
    }

    public class WhenAnEntryDoesNotExist {

        public void itsMetadataCanNotBeRead() {
            specify(new Block() {
                public void run() throws Throwable {
                    table.readMetadata(key, PROPERTY);
                }
            }, should.raise(IllegalArgumentException.class));
        }

        public void itsMetadataCanNotBeUpdated() {
            specify(new Block() {
                public void run() throws Throwable {
                    table.updateMetadata(key, PROPERTY, value);
                }
            }, should.raise(IllegalArgumentException.class));
        }

        public void itsMetadataCanNotBeDeleted() {
            specify(new Block() {
                public void run() throws Throwable {
                    table.deleteMetadata(key, PROPERTY);
                }
            }, should.raise(IllegalArgumentException.class));
        }
    }
}
