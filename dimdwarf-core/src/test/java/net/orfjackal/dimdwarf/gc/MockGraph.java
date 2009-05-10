// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.gc;

import net.orfjackal.dimdwarf.util.Objects;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.swing.event.EventListenerList;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author Esko Luontola
 * @since 29.11.2008
 */
@ThreadSafe
public class MockGraph implements Graph<String> {

    private final Map<String, MockNode> nodes = new ConcurrentSkipListMap<String, MockNode>();
    private final MockNode root = new MockNode();
    private final EventListenerList listeners = new EventListenerList();

    public Iterable<String> getAllNodes() {
        return Collections.unmodifiableCollection(nodes.keySet());
    }

    public Iterable<String> getRootNodes() {
        return Collections.unmodifiableCollection(root.edges);
    }

    public Iterable<String> getConnectedNodesOf(String node) {
        return Collections.unmodifiableCollection(getNode(node).edges);
    }

    public void createNode(String node) {
        nodes.put(node, new MockNode());
        fireNodeCreated(node);
    }

    public void removeNode(String node) {
        for (String target : getConnectedNodesOf(node)) {
            fireReferenceRemoved(node, target);
        }
        nodes.remove(node);
        root.edges.remove(node);
    }

    public void createDirectedEdge(@Nullable String source, String target) {
        getNode(source).edges.add(target);
        fireReferenceCreated(source, target);
    }

    public void removeDirectedEdge(@Nullable String source, String target) {
        getNode(source).edges.remove(target);
        fireReferenceRemoved(source, target);
    }

    private void fireNodeCreated(String node) {
        for (MutatorListener<String> listener : getMutatorListeners()) {
            listener.onNodeCreated(node);
        }
    }

    private void fireReferenceCreated(String source, String target) {
        for (MutatorListener<String> listener : getMutatorListeners()) {
            listener.onReferenceCreated(source, target);
        }
    }

    private void fireReferenceRemoved(String source, String target) {
        for (MutatorListener<String> listener : getMutatorListeners()) {
            listener.onReferenceRemoved(source, target);
        }
    }

    public byte[] getMetadata(String node, String metaKey) {
        byte[] bytes = getNode(node).metadata.get(metaKey);
        return bytes != null ? bytes.clone() : new byte[0];
    }

    public void setMetadata(String node, String metaKey, byte[] metaValue) {
        getNode(node).metadata.put(metaKey, metaValue.clone());
    }

    private MockNode getNode(@Nullable String node) {
        if (node == null) {
            return root;
        }
        MockNode n = nodes.get(node);
        if (n == null) {
            n = new MockNode();
        }
        return n;
    }

    public void addMutatorListener(MutatorListener<String> listener) {
        listeners.add(MutatorListener.class, listener);
    }

    public void removeMutatorListener(MutatorListener<String> listener) {
        listeners.remove(MutatorListener.class, listener);
    }

    private MutatorListener<String>[] getMutatorListeners() {
        return Objects.uncheckedCast(listeners.getListeners(MutatorListener.class));
    }


    private static class MockNode {
        public final Map<String, byte[]> metadata = new ConcurrentHashMap<String, byte[]>();
        public final List<String> edges = new CopyOnWriteArrayList<String>();
    }
}
