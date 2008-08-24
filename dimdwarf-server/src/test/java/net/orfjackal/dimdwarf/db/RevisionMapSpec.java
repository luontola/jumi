/*
 * Dimdwarf Application Server
 * Copyright (c) 2008, Esko Luontola
 * All Rights Reserved.
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
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Esko Luontola
 * @since 20.8.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class RevisionMapSpec extends Specification<Object> {

    private RevisionMap<String, String> map = new RevisionMap<String, String>();

    public class RevisionsOfARevisionMap {

        public Object create() {
            return null;
        }

        public void startsFromFirstRevision() {
            specify(map.getCurrentRevision(), should.equal(0));
            specify(map.getOldestRevision(), should.equal(0));
        }

        public void increasesCurrentRevisionOnIncrement() {
            map.incrementRevision();
            specify(map.getCurrentRevision(), should.equal(1));
            specify(map.getOldestRevision(), should.equal(0));
        }

        public void increasesOldestRevisionOnPurge() {
            map.incrementRevision();
            map.purgeRevisionsOlderThan(1);
            specify(map.getCurrentRevision(), should.equal(1));
            specify(map.getOldestRevision(), should.equal(1));
        }

        public void oldestRevisionIsAtMostTheCurrentRevision() {
            map.incrementRevision();
            map.purgeRevisionsOlderThan(2);
            specify(map.getCurrentRevision(), should.equal(1));
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
            specify(map.get("key", map.getCurrentRevision()), should.equal(null));
        }
    }

    public class WhenAValueIsAdded {

        private long revision;

        public Object create() {
            map.incrementRevision();
            map.put("key", "value");
            revision = map.getCurrentRevision();
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
            map.incrementRevision();
            map.put("key", "old");
            beforeUpdate = map.getCurrentRevision();

            map.incrementRevision();
            map.put("key", "new");
            afterUpdate = map.getCurrentRevision();
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
            map.incrementRevision();
            map.put("key", "value");
            beforeRemove = map.getCurrentRevision();
            map.incrementRevision();
            map.remove("key");
            afterRemove = map.getCurrentRevision();
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

    public class IteratingOverARevision {

        private List<String> keys = new ArrayList<String>();
        private List<String> values = new ArrayList<String>();

        public Object create() {
            map.incrementRevision();
            map.put("a", "AA");
            map.put("b", "BB");
            map.put("c", "X");
            map.incrementRevision();
            map.remove("c");
            long revision = map.getCurrentRevision();
            map.incrementRevision();
            map.put("b", "Y");

            Iterator<Map.Entry<String, String>> it = map.iterator(revision);
            while (it.hasNext()) {
                Map.Entry<String, String> e = it.next();
                keys.add(e.getKey());
                values.add(e.getValue());
            }
            return null;
        }

        public void iteratesOverAllValuesInTheRevision() {
            specify(keys, should.containInOrder("a", "b"));
            specify(values, should.containInOrder("AA", "BB"));
        }

        public void doesNotIterateOverValuesOfOtherRevisions() {
            specify(keys, should.not().contain("c"));
            specify(values, should.not().contain("X"));
            specify(values, should.not().contain("Y"));
        }
    }
}
