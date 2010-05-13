// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db.inmemory;

import jdave.*;
import jdave.junit4.JDaveRunner;
import org.junit.runner.RunWith;

import java.util.*;

@RunWith(JDaveRunner.class)
@Group({"fast"})
public class RevisionMapIteratorSpec extends Specification<Object> {

    private RevisionMap<String, String> map = new RevisionMap<String, String>();
    private List<String> keys = new ArrayList<String>();
    private List<String> values = new ArrayList<String>();

    private void readFully(long readRevision) {
        Iterator<Map.Entry<String, String>> it = new RevisionMapIterator<String, String>(map, readRevision);
        while (it.hasNext()) {
            Map.Entry<String, String> e = it.next();
            keys.add(e.getKey());
            values.add(e.getValue());
        }
    }


    public class IteratingOverARevision {

        private long writeRevision;
        private long readRevision;

        public void create() {
            writeRevision = 1;
            map.put("a", "AA", writeRevision);
            map.put("b", "BB", writeRevision);
            map.put("c", "X", writeRevision);

            writeRevision++;
            map.remove("c", writeRevision);
            readRevision = writeRevision;

            writeRevision++;
            map.put("b", "Y", writeRevision);
        }

        public void iteratesOverAllValuesInTheRevision() {
            readFully(readRevision);
            specify(keys, should.containInPartialOrder("a", "b"));
            specify(values, should.containInPartialOrder("AA", "BB"));
        }

        public void doesNotIterateOverValuesOfOtherRevisions() {
            readFully(readRevision);
            specify(keys, should.not().contain("c"));
            specify(values, should.not().contain("X"));
            specify(values, should.not().contain("Y"));
        }

        public void concurrentModificationInOtherTransactionsIsAllowed() {
            Iterator<Map.Entry<String, String>> it = new RevisionMapIterator<String, String>(map, readRevision);
            assert writeRevision > readRevision;
            map.put("newkey", "Z", writeRevision);
            specify(it.hasNext()); // should not throw ConcurrentModificationException
            specify(it.next(), should.not().equal(null)); // the above modification can not be seen in older revisions
        }

        public void iteratorStopsWhenAllValuesHaveBeenIterated() {
            final Iterator<Map.Entry<String, String>> it = new RevisionMapIterator<String, String>(map, readRevision);
            specify(it.hasNext());
            it.next();
            specify(it.hasNext());
            it.next();
            specify(!it.hasNext());
            specify(new Block() {
                public void run() throws Throwable {
                    it.next();
                }
            }, should.raise(NoSuchElementException.class));
        }
    }
}
