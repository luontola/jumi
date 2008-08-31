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

import jdave.Group;
import jdave.Specification;
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

        public Object create() {
            list = new RevisionList<String>(1, "one");
            return null;
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

        public Object create() {
            RevisionList<String> previous = new RevisionList<String>(1, "one");
            list = new RevisionList<String>(2, "two", previous);
            return null;
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

        public Object create() {
            RevisionList<String> previous = new RevisionList<String>(1, "one");
            list = new RevisionList<String>(3, "three", previous);
            return null;
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

        public Object create() {
            tail = new RevisionList<String>(1, "one");
            RevisionList<String> two = new RevisionList<String>(2, "two", tail);
            RevisionList<String> four = new RevisionList<String>(4, "four", two);
            list = new RevisionList<String>(5, null, four);
            return null;
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
