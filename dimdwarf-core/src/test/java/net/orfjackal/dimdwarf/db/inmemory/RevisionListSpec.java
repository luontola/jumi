// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db.inmemory;

import jdave.*;
import jdave.junit4.JDaveRunner;
import org.junit.runner.RunWith;

/**
 * @author Esko Luontola
 * @since 19.8.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class RevisionListSpec extends Specification<Object> {

    public class WhenThereIsOnlyOneRevision {

        private RevisionList<String> list;

        public void create() {
            list = new RevisionList<String>(1, "one");
        }

        public void thatRevisionCanBeRead() {
            specify(list.get(1), should.equal("one"));
        }

        public void olderRevisionsDoNotExist() {
            specify(list.get(0), should.equal(null));
        }

        public void newerRevisionsFallBackToTheNewestAvailableRevision() {
            specify(list.get(2), should.equal("one"));
        }
    }

    public class WhenThereAreManySequentialRevisions {

        private RevisionList<String> list;

        public void create() {
            RevisionList<String> previous = new RevisionList<String>(1, "one");
            list = new RevisionList<String>(2, "two", previous);
        }

        public void theLatestRevisionCanBeRead() {
            specify(list.get(2), should.equal("two"));
        }

        public void thePreviousRevisionCanBeRead() {
            specify(list.get(1), should.equal("one"));
        }
    }

    public class WhenThereAreManySparseRevisions {

        private RevisionList<String> list;

        public void create() {
            RevisionList<String> previous = new RevisionList<String>(1, "one");
            list = new RevisionList<String>(3, "three", previous);
        }

        public void theLatestRevisionCanBeRead() {
            specify(list.get(3), should.equal("three"));
        }

        public void inBetweenRevisionsFallBackToThePreviousRevision() {
            specify(list.get(2), should.equal("one"));
        }

        public void theOldestRevisionCanBeRead() {
            specify(list.get(1), should.equal("one"));
        }
    }

    public class WhenOldRevisionsArePurged {

        private RevisionList<String> tail;
        private RevisionList<String> list;

        public void create() {
            tail = new RevisionList<String>(1, "one");
            RevisionList<String> two = new RevisionList<String>(2, "two", tail);
            RevisionList<String> four = new RevisionList<String>(4, "four", two);
            list = new RevisionList<String>(5, null, four);
        }

        public void purgingAlreadyPurgedRevisionsHasNoEffect() {
            list.purgeRevisionsOlderThan(0);
            list.purgeRevisionsOlderThan(1);
            specify(list.get(1), should.equal("one"));
        }

        public void sequentialRevisionsCanBePurged() {
            list.purgeRevisionsOlderThan(2);
            specify(list.get(1), should.equal(null));
            specify(list.get(2), should.equal("two"));
        }

        public void sparseRevisionsCanBePurged() {
            list.purgeRevisionsOlderThan(3);
            specify(list.get(1), should.equal(null));
            specify(list.get(2), should.equal("two"));
            specify(list.get(3), should.equal("two"));
            specify(list.get(4), should.equal("four"));
        }

        public void anOnlyNonNullRevisionCanNotBePurged() {
            tail.purgeRevisionsOlderThan(5);
            specify(tail.get(1), should.equal("one"));
            specify(tail.isEmpty(), should.equal(false));
        }

        public void theListCanBeDiscardedIfAfterPurgeItContainsOnlyANullRevision() {
            specify(list.get(5), should.equal(null));
            specify(list.isEmpty(), should.equal(false));
            list.purgeRevisionsOlderThan(5);
            specify(list.get(4), should.equal(null));
            specify(list.get(5), should.equal(null));
            specify(list.isEmpty(), should.equal(true));
        }
    }
}
