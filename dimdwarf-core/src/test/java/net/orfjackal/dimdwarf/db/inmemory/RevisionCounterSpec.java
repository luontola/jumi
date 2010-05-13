// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db.inmemory;

import jdave.*;
import jdave.junit4.JDaveRunner;
import org.junit.runner.RunWith;

@RunWith(JDaveRunner.class)
@Group({"fast"})
public class RevisionCounterSpec extends Specification<Object> {

    private RevisionCounter counter = new RevisionCounter();

    private void specifyReadableRange(long from, long to) {
        specify(counter.getOldestReadableRevision(), should.equal(from));
        specify(counter.getNewestReadableRevision(), should.equal(to));
    }


    public class WhenNoRevisionsHaveBeenAccessed {

        public void noRevisionsAreInUse() {
            specify(counter.getRevisionsInUse(), should.containExactly());
        }

        public void theReadableRevisionsAreZeroToZero() {
            specifyReadableRange(0L, 0L);
        }
    }

    public class WhenAUserAccessesTheNewestRevision {
        private RevisionHandle handle;

        public void create() {
            handle = counter.openNewestRevision();
        }

        public void theUserGetsToReadTheNewestReadableRevision() {
            specify(handle.getReadRevision(), should.equal(0L));
        }

        public void theUserDoesNotYetKnowTheWriteRevision() {
            specify(new Block() {
                public void run() throws Throwable {
                    handle.getWriteRevision();
                }
            }, should.raise(IllegalStateException.class));
        }

        public void onRollbackTheChangesAreCancelled() {
            handle.rollback();
            specify(counter.getRevisionsInUse(), should.containExactly());
            specifyReadableRange(0L, 0L);
        }

        public void theReadRevisionIsInUse() {
            specify(counter.getRevisionsInUse(), should.containExactly(0L));
        }

        public void theReadableRevisionsAreUnchanged() {
            specifyReadableRange(0L, 0L);
        }
    }

    public class WhenAUserPreparesForWriting {
        private RevisionHandle handle;
        private long writeRevision;

        public void create() {
            handle = counter.openNewestRevision();
            handle.prepareWriteRevision();
            writeRevision = handle.getWriteRevision();
        }

        public void theUserGetsANewWriteRevision() {
            specify(writeRevision, should.equal(1L));
        }

        public void onRollbackTheChangesAreCancelled() {
            handle.rollback();
            specify(counter.getRevisionsInUse(), should.containExactly());
            specifyReadableRange(0L, 0L);
        }

        public void theReadRevisionIsStillInUse() {
            specify(counter.getRevisionsInUse(), should.containExactly(0L));
        }

        public void theReadableRevisionsAreUnchanged() {
            specifyReadableRange(0L, 0L);
        }
    }

    public class WhenAUserCommitsHisWrites {
        private RevisionHandle handle;
        private long writeRevision;

        public void create() {
            handle = counter.openNewestRevision();
            handle.prepareWriteRevision();
            writeRevision = handle.getWriteRevision();
            handle.commitWrites();
        }

        public void theReadRevisionIsNoMoreInUse() {
            specify(counter.getRevisionsInUse(), should.containExactly());
        }

        public void theReadableRevisionsAreUpdated() {
            specifyReadableRange(writeRevision, writeRevision);
        }

        public void theNextUserWillGetToReadTheNewestReadableRevision() {
            RevisionHandle handle2 = counter.openNewestRevision();
            specify(handle2.getReadRevision(), should.equal(writeRevision));
        }
    }

    public class WhenThereAreManyConcurrentUsersAccessingTheSameRevision {
        private RevisionHandle handle1;
        private RevisionHandle handle2;

        public void create() {
            handle1 = counter.openNewestRevision();
            handle2 = counter.openNewestRevision();
        }

        public void theyAllCanReadTheSameRevision() {
            specify(handle1.getReadRevision(), should.equal(0L));
            specify(handle2.getReadRevision(), should.equal(0L));
            specify(counter.getRevisionsInUse(), should.containExactly(0L, 0L));
        }

        public void onPrepareTheyGetDifferentWriteRevisions() {
            handle1.prepareWriteRevision();
            handle2.prepareWriteRevision();
            specify(handle1.getWriteRevision(), should.equal(1L));
            specify(handle2.getWriteRevision(), should.equal(2L));
        }

        public void theWriteRevisionsAreGivenInTheOrderOfPrepare() {
            handle2.prepareWriteRevision();
            handle1.prepareWriteRevision();
            specify(handle2.getWriteRevision(), should.equal(1L));
            specify(handle1.getWriteRevision(), should.equal(2L));
        }

        public void whenOnlyOneCommitsTheReadableRangeConsistsOfThePreviousAndTheNewRevisions() {
            handle1.prepareWriteRevision();
            handle1.commitWrites();
            specifyReadableRange(0L, 1L);
        }

        public void whenBothCommitTheReadableRangeConsistsOfTheNewestRevision() {
            handle1.prepareWriteRevision();
            handle1.commitWrites();
            handle2.prepareWriteRevision();
            handle2.commitWrites();
            specifyReadableRange(2L, 2L);
        }

        public void ifTheFirstToPrepareCommitsLastThenTheReadableRangeWillNotExposeTheNonCommittedRevision() {
            handle1.prepareWriteRevision();
            handle2.prepareWriteRevision();
            specifyReadableRange(0L, 0L);
            handle2.commitWrites();
            specifyReadableRange(0L, 0L);
            handle1.commitWrites();
            specifyReadableRange(2L, 2L);
        }

        public void theNextUserAlwaysGetsToReadTheNewestReadableRevision() {
            handle1.prepareWriteRevision();
            handle1.commitWrites();
            specify(counter.openNewestRevision().getReadRevision(), should.equal(1L));
            handle2.prepareWriteRevision();
            handle2.commitWrites();
            specify(counter.openNewestRevision().getReadRevision(), should.equal(2L));
        }
    }
}
