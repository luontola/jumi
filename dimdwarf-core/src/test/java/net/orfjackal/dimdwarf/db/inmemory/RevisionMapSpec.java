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

import jdave.*;
import jdave.junit4.JDaveRunner;
import org.junit.runner.RunWith;

/**
 * @author Esko Luontola
 * @since 20.8.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class RevisionMapSpec extends Specification<Object> {

    private static final long FIRST_REVISION = 0;

    private RevisionMap<String, String> map = new RevisionMap<String, String>();
    private long writeRevision = FIRST_REVISION;


    public class AnEmptyRevisionMap {

        public void isEmpty() {
            specify(map.size(), should.equal(0));
        }

        public void doesNotContainAnyValues() {
            specify(map.get("key", 0), should.equal(null));
        }

        public void valuesCanBeAddedToOnlyPositiveRevisions() {
            specify(new Block() {
                public void run() throws Throwable {
                    map.put("key", "value", -1);
                }
            }, should.raise(IllegalArgumentException.class));
            specify(new Block() {
                public void run() throws Throwable {
                    map.put("key", "value", 0);
                }
            }, should.raise(IllegalArgumentException.class));
            map.put("key", "value", 1);
        }
    }

    public class WhenAValueIsAdded {

        private long beforeAdd;
        private long afterAdd;

        public void create() {
            beforeAdd = writeRevision;

            writeRevision++;
            map.put("key", "value", writeRevision);
            afterAdd = writeRevision;
        }

        public void theMapIsNotEmpty() {
            specify(map.size(), should.equal(1));
        }

        public void theValueExistsOnCurrentRevision() {
            specify(map.get("key", afterAdd), should.equal("value"));
        }

        public void theValueExistsOnFutureRevisions() {
            specify(map.get("key", afterAdd + 1), should.equal("value"));
        }

        public void theValueDoesNotExistOnPreviousRevisions() {
            specify(map.get("key", beforeAdd), should.equal(null));
        }

        public void theOnlyRevisionOfAValueCanNotBePurged() {
            map.purgeRevisionsOlderThan(afterAdd + 1);
            specify(map.size(), should.equal(1));
            specify(map.get("key", afterAdd), should.equal("value"));
        }
    }

    public class WhenAnExistingValueIsUpdated {

        private long beforeUpdate;
        private long afterUpdate;

        public void create() {
            writeRevision++;
            map.put("key", "old", writeRevision);
            beforeUpdate = writeRevision;

            writeRevision++;
            map.put("key", "new", writeRevision);
            afterUpdate = writeRevision;
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
                    map.put("key", "even newer", afterUpdate);
                }
            }, should.raise(IllegalArgumentException.class, "Key key already modified in revision " + afterUpdate));
            specify(map.get("key", afterUpdate), should.equal("new"));
        }

        public void aValueCanNotBeUpdatedDuringAnOlderRevision() {
            specify(new Block() {
                public void run() throws Throwable {
                    map.put("key", "old revision", beforeUpdate);
                }
            }, should.raise(IllegalArgumentException.class, "Key key already modified in revision " + afterUpdate));
            specify(map.get("key", afterUpdate), should.equal("new"));
        }

        public void aDifferentValueCanBeUpdatedDuringAnOlderRevision() {
            map.put("key2", "new2", beforeUpdate);
            specify(map.get("key2", beforeUpdate), should.equal("new2"));
            specify(map.get("key2", afterUpdate), should.equal("new2"));
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

        public void create() {
            writeRevision++;
            map.put("key", "value", writeRevision);
            beforeRemove = writeRevision;

            writeRevision++;
            map.remove("key", writeRevision);
            afterRemove = writeRevision;
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

        public void create() {
            writeRevision++;
            map.put("a", "A", writeRevision);
            map.put("c", "C", writeRevision);
        }

        public void firstKey() {
            specify(map.firstKey(), should.equal("a"));
        }

        public void firstKeyOfEmptySet() {
            map = new RevisionMap<String, String>();
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
}
