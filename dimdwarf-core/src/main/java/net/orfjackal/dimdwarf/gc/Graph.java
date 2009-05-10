// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.gc;

/**
 * @author Esko Luontola
 * @since 29.11.2008
 */
public interface Graph<T> {

    Iterable<T> getAllNodes();

    Iterable<T> getRootNodes();

    Iterable<T> getConnectedNodesOf(T node);

    void removeNode(T node);

    byte[] getMetadata(T node, String metaKey);

    void setMetadata(T node, String metaKey, byte[] metaValue);
}
