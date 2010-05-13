// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db.inmemory;

import jdave.*;
import jdave.junit4.JDaveRunner;
import org.junit.runner.RunWith;

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
            specify(map.exists("key", afterAdd));
            specify(map.get("key", afterAdd), should.equal("value"));
        }

        public void theValueExistsOnFutureRevisions() {
            specify(map.exists("key", afterAdd + 1));
            specify(map.get("key", afterAdd + 1), should.equal("value"));
        }

        public void theValueDoesNotExistOnPreviousRevisions() {
            specify(!map.exists("key", beforeAdd));
            specify(map.get("key", beforeAdd), should.equal(null));
        }

        public void theKeyExistsOnCurrentRevision() {
            specify(map.firstKey(afterAdd), should.equal("key"));
            specify(map.nextKeyAfter("a", afterAdd), should.equal("key"));
        }

        public void theKeyExistsOnFutureRevisions() {
            specify(map.firstKey(afterAdd + 1), should.equal("key"));
            specify(map.nextKeyAfter("a", afterAdd + 1), should.equal("key"));
        }

        public void theKeyDoesNotExistOnPreviousRevisions() {
            specify(map.firstKey(beforeAdd), should.equal(null));
            specify(map.nextKeyAfter("a", beforeAdd), should.equal(null));
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
            specify(!map.exists("key", afterRemove));
            specify(map.get("key", afterRemove), should.equal(null));
        }

        public void theValueStillExistsInThePreviousRevision() {
            specify(map.exists("key", beforeRemove));
            specify(map.get("key", beforeRemove), should.equal("value"));
        }

        public void theKeyDoesNotExistInTheCurrentRevision() {
            specify(map.firstKey(afterRemove), should.equal(null));
            specify(map.nextKeyAfter("a", afterRemove), should.equal(null));
        }

        public void theKeyStillExistsInThePreviousRevision() {
            specify(map.firstKey(beforeRemove), should.equal("key"));
            specify(map.nextKeyAfter("a", beforeRemove), should.equal("key"));
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
            specify(map.firstKey(writeRevision), should.equal("a"));
        }

        public void firstKeyOfEmptySet() {
            map = new RevisionMap<String, String>();
            specify(map.firstKey(writeRevision), should.equal(null));
        }

        public void nextKeyAfterExistingKey() {
            specify(map.nextKeyAfter("a", writeRevision), should.equal("c"));
        }

        public void nextKeyAfterNonexistentKey() {
            specify(map.nextKeyAfter("b", writeRevision), should.equal("c"));
        }

        public void nextKeyAfterLastKey() {
            specify(map.nextKeyAfter("c", writeRevision), should.equal(null));
        }

        public void nextKeyAfterNonexistentKeyAfterLastKey() {
            specify(map.nextKeyAfter("d", writeRevision), should.equal(null));
        }
    }
}
