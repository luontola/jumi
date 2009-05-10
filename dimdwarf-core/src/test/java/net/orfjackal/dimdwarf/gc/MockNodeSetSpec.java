// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.gc;

import jdave.*;
import jdave.junit4.JDaveRunner;
import org.junit.runner.RunWith;

import java.util.*;

/**
 * @author Esko Luontola
 * @since 12.12.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class MockNodeSetSpec extends Specification<Object> {

    private MockNodeSet set = new MockNodeSet();


    public class WhenNodeSetIsEmpty {

        public void itContainsNoNodes() {
            specify(set.pollFirst(), should.equal(null));
        }
    }

    public class WhenNodesHaveBeenAddedToNodeSet {

        public void create() {
            set.add("A");
            set.add("B");
        }

        public void thoseNodesCanBeTakenFromIt() {
            List<String> taken = new ArrayList<String>();
            taken.add(set.pollFirst());
            taken.add(set.pollFirst());
            specify(taken, should.containExactly("A", "B"));
            specify(set.pollFirst(), should.equal(null));
        }
    }
}
