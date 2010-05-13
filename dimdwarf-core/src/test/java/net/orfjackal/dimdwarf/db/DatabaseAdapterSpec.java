// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db;

import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.api.internal.EntityObjectId;
import net.orfjackal.dimdwarf.entities.dao.ConvertEntityIdToBytes;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

import java.io.UnsupportedEncodingException;
import java.util.*;

import static net.orfjackal.dimdwarf.util.Objects.uncheckedCast;

@RunWith(JDaveRunner.class)
@Group({"fast"})
public class DatabaseAdapterSpec extends Specification<Object> {

    private Database<Blob, Blob> db;
    private DatabaseTable<Blob, Blob> table;
    private Database<String, EntityId> dbAdapter;
    private DatabaseTable<String, EntityId> tableAdapter;

    private String key = "key";
    private EntityId value = new EntityObjectId(42);
    private Blob keyBytes;
    private Blob valueBytes;

    public void create() throws Exception {
        db = uncheckedCast(mock(Database.class));
        table = uncheckedCast(mock(DatabaseTable.class));
        dbAdapter = new DatabaseAdapter<String, EntityId, Blob, Blob>(db, new ConvertStringToBytes(), new ConvertEntityIdToBytes());

        keyBytes = Blob.fromBytes(key.getBytes("UTF-8"));
        valueBytes = new ConvertEntityIdToBytes().forth(value);
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
