// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.gc;

/**
 * @author Esko Luontola
 * @since 12.12.2008
 */
public interface NodeSetFactory {

    <T> NodeSet<T> create(String name);
}
