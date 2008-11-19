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
 * @since 12.9.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class RevisionMapIteratorSpec extends Specification<Object> {

    private RevisionMap<String, String> map;
    private RevisionCounter counter;

    public void create() throws Exception {
        counter = new RevisionCounter();
        map = new RevisionMap<String, String>(counter);
    }


    public class IteratingOverARevision {

        private List<String> keys = new ArrayList<String>();
        private List<String> values = new ArrayList<String>();
        private long revision;

        public void create() {
            counter.incrementRevision();
            map.put("a", "AA");
            map.put("b", "BB");
            map.put("c", "X");
            counter.incrementRevision();
            map.remove("c");
            revision = counter.getCurrentRevision();
            counter.incrementRevision();
            map.put("b", "Y");
        }

        private void readFully(long revision) {
            Iterator<Map.Entry<String, String>> it = new RevisionMapIterator<String, String>(map, revision);
            while (it.hasNext()) {
                Map.Entry<String, String> e = it.next();
                keys.add(e.getKey());
                values.add(e.getValue());
            }
        }

        public void iteratesOverAllValuesInTheRevision() {
            readFully(revision);
            specify(keys, should.containInPartialOrder("a", "b"));
            specify(values, should.containInPartialOrder("AA", "BB"));
        }

        public void doesNotIterateOverValuesOfOtherRevisions() {
            readFully(revision);
            specify(keys, should.not().contain("c"));
            specify(values, should.not().contain("X"));
            specify(values, should.not().contain("Y"));
        }

        public void concurrentModificationInOtherTransactionsIsAllowed() {
            Iterator<Map.Entry<String, String>> it = new RevisionMapIterator<String, String>(map, revision);
            map.put("newkey", "Z");
            specify(it.hasNext()); // should not throw ConcurrentModificationException
            specify(it.next(), should.not().equal(null));
        }

        public void iteratorStopsWhenAllValuesHaveBeenIterated() {
            final Iterator<Map.Entry<String, String>> it = new RevisionMapIterator<String, String>(map, revision);
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