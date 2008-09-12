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

import jdave.Block;
import jdave.Group;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.junit.runner.RunWith;

import java.util.*;

/**
 * @author Esko Luontola
 * @since 20.8.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class RevisionMapSpec extends Specification<Object> {

    private RevisionMap<String, String> map;
    private RevisionCounter counter;

    public void create() throws Exception {
        counter = new RevisionCounter();
        map = new RevisionMap<String, String>(counter);
    }

    public class RevisionsOfARevisionMap {

        public Object create() {
            return null;
        }

        public void startsFromFirstRevision() {
            specify(counter.getCurrentRevision(), should.equal(0));
            specify(map.getOldestRevision(), should.equal(0));
        }

        public void increasesCurrentRevisionOnIncrement() {
            counter.incrementRevision();
            specify(counter.getCurrentRevision(), should.equal(1));
            specify(map.getOldestRevision(), should.equal(0));
        }

        public void increasesOldestRevisionOnPurge() {
            counter.incrementRevision();
            map.purgeRevisionsOlderThan(1);
            specify(counter.getCurrentRevision(), should.equal(1));
            specify(map.getOldestRevision(), should.equal(1));
        }

        public void oldestRevisionIsAtMostTheCurrentRevision() {
            counter.incrementRevision();
            map.purgeRevisionsOlderThan(2);
            specify(counter.getCurrentRevision(), should.equal(1));
            specify(map.getOldestRevision(), should.equal(1));
        }
    }

    public class AnEmptyRevisionMap {

        public Object create() {
            return null;
        }

        public void isEmpty() {
            specify(map.size(), should.equal(0));
        }

        public void doesNotContainAnyValues() {
            specify(map.get("key", counter.getCurrentRevision()), should.equal(null));
        }
    }

    public class WhenAValueIsAdded {

        private long revision;

        public Object create() {
            counter.incrementRevision();
            map.put("key", "value");
            revision = counter.getCurrentRevision();
            return null;
        }

        public void theMapIsNotEmpty() {
            specify(map.size(), should.equal(1));
        }

        public void theValueExistsOnCurrentRevision() {
            specify(map.get("key", revision), should.equal("value"));
        }

        public void theValueExistsOnFutureRevisions() {
            specify(map.get("key", revision + 1), should.equal("value"));
        }

        public void theValueDoesNotExistOnPreviousRevision() {
            specify(map.get("key", revision - 1), should.equal(null));
        }

        public void theOnlyRevisionOfAValueCanNotBePurged() {
            map.purgeRevisionsOlderThan(revision + 1);
            specify(map.size(), should.equal(1));
            specify(map.get("key", revision), should.equal("value"));
        }
    }

    public class WhenAnExistingValueIsUpdated {

        private long beforeUpdate;
        private long afterUpdate;

        public Object create() {
            counter.incrementRevision();
            map.put("key", "old");
            beforeUpdate = counter.getCurrentRevision();

            counter.incrementRevision();
            map.put("key", "new");
            afterUpdate = counter.getCurrentRevision();
            return null;
        }

        public void theValueNewValueExistsInTheCurrentRevision() {
            specify(map.get("key", afterUpdate), should.equal("new"));
        }

        public void theOldValueStillExistsInThePreviousRevision() {
            specify(map.get("key", beforeUpdate), should.equal("old"));
        }

        public void aValueCanNotBeUpdatedTwiseDuringTheSameRevision() {
            specify(new Block() {
                public void run() throws Throwable {
                    map.put("key", "even newer");
                }
            }, should.raise(IllegalArgumentException.class));
            specify(map.get("key", afterUpdate), should.equal("new"));
        }

        public void theOldValueCanBePurged() {
            map.purgeRevisionsOlderThan(beforeUpdate);
            specify(map.size(), should.equal(1));
            specify(map.get("key", beforeUpdate), should.equal("old"));

            map.purgeRevisionsOlderThan(afterUpdate);
            specify(map.size(), should.equal(1));
            specify(map.get("key", beforeUpdate), should.equal(null));
        }
    }

    public class WhenAValueIsRemoved {

        private long beforeRemove;
        private long afterRemove;

        public Object create() {
            counter.incrementRevision();
            map.put("key", "value");
            beforeRemove = counter.getCurrentRevision();
            counter.incrementRevision();
            map.remove("key");
            afterRemove = counter.getCurrentRevision();
            return null;
        }

        public void theValueDoesNotExistInTheCurrentRevision() {
            specify(map.get("key", afterRemove), should.equal(null));
        }

        public void theValueStillExistsInThePreviousRevision() {
            specify(map.get("key", beforeRemove), should.equal("value"));
        }

        public void theWholeEntryCanBePurged() {
            map.purgeRevisionsOlderThan(beforeRemove);
            specify(map.size(), should.equal(1));
            specify(map.get("key", beforeRemove), should.equal("value"));

            map.purgeRevisionsOlderThan(afterRemove);
            specify(map.size(), should.equal(0));
            specify(map.get("key", beforeRemove), should.equal(null));
        }
    }

    public class FindingTheNextKey {

        public Object create() {
            counter.incrementRevision();
            map.put("a", "A");
            map.put("c", "C");
            return null;
        }

        public void firstKey() {
            specify(map.firstKey(), should.equal("a"));
        }

        public void firstKeyOfEmptySet() {
            map = new RevisionMap<String, String>(counter);
            specify(map.firstKey(), should.equal(null));
        }

        public void nextKeyAfterExistingKey() {
            specify(map.nextKeyAfter("a"), should.equal("c"));
        }

        public void nextKeyAfterNonexistentKey() {
            specify(map.nextKeyAfter("b"), should.equal("c"));
        }

        public void nextKeyAfterLastKey() {
            specify(map.nextKeyAfter("c"), should.equal(null));
        }

        public void nextKeyAfterNonexistentKeyAfterLastKey() {
            specify(map.nextKeyAfter("d"), should.equal(null));
        }
    }

    public class IteratingOverARevision {

        private List<String> keys = new ArrayList<String>();
        private List<String> values = new ArrayList<String>();
        private long revision;

        public Object create() {
            counter.incrementRevision();
            map.put("a", "AA");
            map.put("b", "BB");
            map.put("c", "X");
            counter.incrementRevision();
            map.remove("c");
            revision = counter.getCurrentRevision();
            counter.incrementRevision();
            map.put("b", "Y");
            return null;
        }

        private void readFully(long revision) {
            Iterator<Map.Entry<String, String>> it = map.iterator(revision);
            while (it.hasNext()) {
                Map.Entry<String, String> e = it.next();
                keys.add(e.getKey());
                values.add(e.getValue());
            }
        }

        public void iteratesOverAllValuesInTheRevision() {
            readFully(revision);
            specify(keys, should.containInOrder("a", "b"));
            specify(values, should.containInOrder("AA", "BB"));
        }

        public void doesNotIterateOverValuesOfOtherRevisions() {
            readFully(revision);
            specify(keys, should.not().contain("c"));
            specify(values, should.not().contain("X"));
            specify(values, should.not().contain("Y"));
        }

        public void concurrentModificationInOtherTransactionsIsAllowed() {
            Iterator<Map.Entry<String, String>> it = map.iterator(revision);
            map.put("newkey", "Z");
            specify(it.hasNext()); // should not throw ConcurrentModificationException
            specify(it.next(), should.not().equal(null));
        }

        public void iteratorStopsWhenAllValuesHaveBeenIterated() {
            final Iterator<Map.Entry<String, String>> it = map.iterator(revision);
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
