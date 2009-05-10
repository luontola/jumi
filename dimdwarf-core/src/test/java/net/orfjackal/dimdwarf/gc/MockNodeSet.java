// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.gc;

import javax.annotation.concurrent.ThreadSafe;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author Esko Luontola
 * @since 12.12.2008
 */
@ThreadSafe
public class MockNodeSet implements NodeSet<String> {

    private NavigableSet<String> nodes = new ConcurrentSkipListSet<String>();

    public void add(String node) {
        nodes.add(node);
    }

    public String pollFirst() {
        return nodes.pollFirst();
    }
}
