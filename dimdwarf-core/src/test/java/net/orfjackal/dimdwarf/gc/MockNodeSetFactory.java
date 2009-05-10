// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.gc;

import net.orfjackal.dimdwarf.util.Objects;

/**
 * @author Esko Luontola
 * @since 12.12.2008
 */
public class MockNodeSetFactory implements NodeSetFactory {

    public <T> NodeSet<T> create(String name) {
        return Objects.uncheckedCast(new MockNodeSet());
    }
}
