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
import net.orfjackal.dimdwarf.tx.TransactionCoordinator;
import net.orfjackal.dimdwarf.tx.TransactionImpl;
import org.junit.runner.RunWith;

/**
 * @author Esko Luontola
 * @since 11.9.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class MultipleDatabaseTablesSpec extends Specification<Object> {

    private static final String TABLE1 = "table1";
    private static final String TABLE2 = "table2";

    private TransactionCoordinator tx;
    private Database db;
    private DatabaseTable table1;
    private DatabaseTable table2;

    private Blob key;
    private Blob value1;
    private Blob value2;

    public void create() throws Exception {
        InMemoryDatabase db = new InMemoryDatabase(TABLE1, TABLE2);
        tx = new TransactionImpl();
        this.db = db.openConnection(tx.getTransaction());
        table1 = this.db.openTable(TABLE1);
        table2 = this.db.openTable(TABLE2);

        key = Blob.fromBytes(new byte[]{0});
        value1 = Blob.fromBytes(new byte[]{1});
        value2 = Blob.fromBytes(new byte[]{2});
    }


    public class OpeningDatabaseTables {

        public Object create() {
            return null;
        }

        public void theSameNamesWillCorrespondTheSameTable() {
            specify(db.openTable(TABLE1), should.equal(table1));
        }

        public void differentNamesWillCorrespondDifferentTables() {
            specify(table1, should.not().equal(table2));
        }

        public void nonexistantTablesCanNotBeOpened() {
            specify(new Block() {
                public void run() throws Throwable {
                    db.openTable("doesNotExist");
                }
            }, should.raise(IllegalArgumentException.class));
        }
    }
}
