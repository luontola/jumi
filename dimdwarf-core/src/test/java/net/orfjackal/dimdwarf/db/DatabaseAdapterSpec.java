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
import static net.orfjackal.dimdwarf.util.Objects.uncheckedCast;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.*;

/**
 * @author Esko Luontola
 * @since 12.9.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class DatabaseAdapterSpec extends Specification<Object> {

    private Database<Blob, Blob> db;
    private DatabaseTable<Blob, Blob> table;
    private Database<String, BigInteger> dbAdapter;
    private DatabaseTable<String, BigInteger> tableAdapter;

    private String key;
    private BigInteger value;
    private Blob keyBytes;
    private Blob valueBytes;

    public void create() throws Exception {
        db = uncheckedCast(mock(Database.class));
        table = uncheckedCast(mock(DatabaseTable.class));
        dbAdapter = new DatabaseAdapter<String, BigInteger, Blob, Blob>(db, new ConvertStringToBytes(), new ConvertBigIntegerToBytes());

        key = "key";
        value = BigInteger.TEN;
        keyBytes = Blob.fromBytes(key.getBytes("UTF-8"));
        valueBytes = new ConvertBigIntegerToBytes().forth(value);
    }

    public class ADatabaseAdapter {

        public void delegatesTables() {
            checking(new Expectations() {{
                one(db).getTableNames(); will(returnValue(new HashSet<String>(Arrays.asList("test"))));
            }});
            specify(dbAdapter.getTableNames(), should.containExactly("test"));
        }

        public void delegatesOpeningTables() {
            checking(new Expectations() {{
                one(db).openTable("test"); will(returnValue(table));
            }});
            specify(dbAdapter.openTable("test"), should.not().equal(null));
        }
    }

    public class ADatabaseTableAdapter {

        public void create() {
            checking(new Expectations() {{
                one(db).openTable("test"); will(returnValue(table));
            }});
            tableAdapter = dbAdapter.openTable("test");
        }

        public void convertsExistenceChecks() {
            checking(new Expectations() {{
                one(table).exists(keyBytes); will(returnValue(true));
            }});
            specify(tableAdapter.exists(key), should.equal(true));
        }

        public void convertsReads() {
            checking(new Expectations() {{
                one(table).read(keyBytes); will(returnValue(valueBytes));
            }});
            specify(tableAdapter.read(key), should.equal(value));
        }

        public void convertsUpdates() {
            checking(new Expectations() {{
                one(table).update(keyBytes, valueBytes);
            }});
            tableAdapter.update(key, value);
        }

        public void convertsDeletes() {
            checking(new Expectations() {{
                one(table).delete(keyBytes);
            }});
            tableAdapter.delete(key);
        }

        public void convertsFirstKey() {
            checking(new Expectations() {{
                one(table).firstKey(); will(returnValue(keyBytes));
            }});
            specify(tableAdapter.firstKey(), should.equal(key));
        }

        public void convertsNextKeyAfter() throws UnsupportedEncodingException {
            String key2 = "key2";
            final Blob key2Bytes = Blob.fromBytes(key2.getBytes("UTF-8"));
            checking(new Expectations() {{
                one(table).nextKeyAfter(keyBytes); will(returnValue(key2Bytes));
            }});
            specify(tableAdapter.nextKeyAfter(key), should.equal(key2));
        }
    }
}
