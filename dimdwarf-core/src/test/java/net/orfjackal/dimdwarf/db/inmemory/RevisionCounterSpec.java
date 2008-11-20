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
 * @since 20.11.2008
 */
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

        public void theUserGetsToReadTheNewestRevision() {
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
            handle.prepareForWrite();
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
            handle.prepareForWrite();
            writeRevision = handle.getWriteRevision();
            handle.commitWrites();
        }

        public void theReadRevisionIsNoMoreInUse() {
            specify(counter.getRevisionsInUse(), should.containExactly());
        }

        public void theReadableRevisionsAreUpdated() {
            specifyReadableRange(writeRevision, writeRevision);
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
            handle1.prepareForWrite();
            handle2.prepareForWrite();
            specify(handle1.getWriteRevision(), should.equal(1L));
            specify(handle2.getWriteRevision(), should.equal(2L));
        }

        public void theWriteRevisionsAreGivenInTheOrderOfPrepare() {
            handle2.prepareForWrite();
            handle1.prepareForWrite();
            specify(handle2.getWriteRevision(), should.equal(1L));
            specify(handle1.getWriteRevision(), should.equal(2L));
        }

        public void whenOnlyOneCommitsTheReadableRangeConsistsOfThePreviousAndTheNewRevisions() {
            handle1.prepareForWrite();
            handle1.commitWrites();
            specifyReadableRange(0L, 1L);
        }

        public void whenBothCommitTheReadableRangeConsistsOfTheNewestRevision() {
            handle1.prepareForWrite();
            handle1.commitWrites();
            handle2.prepareForWrite();
            handle2.commitWrites();
            specifyReadableRange(2L, 2L);
        }

        public void ifTheFirstToPrepareCommitsLastThenTheReadableRangeWillNotExposeTheNonCommittedRevision() {
            handle1.prepareForWrite();
            handle2.prepareForWrite();
            specifyReadableRange(0L, 0L);
            handle2.commitWrites();
            specifyReadableRange(0L, 0L);
            handle1.commitWrites();
            specifyReadableRange(2L, 2L);
        }
    }
}
