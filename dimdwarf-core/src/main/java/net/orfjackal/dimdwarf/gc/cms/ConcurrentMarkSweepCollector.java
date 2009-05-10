// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.gc.cms;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.gc.*;
import net.orfjackal.dimdwarf.tasks.util.IncrementalTask;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;

/**
 * Uses a variation of the mark-sweep collector algorithm presented in
 * <a href="http://portal.acm.org/citation.cfm?id=359642.359655">On-the-fly garbage collection: an exercise in
 * cooperation</a> (Dijkstra et al. 1978)
 *
 * @author Esko Luontola
 * @since 29.11.2008
 */
public class ConcurrentMarkSweepCollector<T> implements GarbageCollector<T>, Serializable {
    private static final long serialVersionUID = 1L;

    private static final String COLOR_KEY = "cms-color";
    private static final String GRAY_NODES_KEY = "cms-gray-nodes";

    private final Graph<T> graph;
    private final NodeSet<T> grayNodes;

    @Inject
    public ConcurrentMarkSweepCollector(Graph<T> graph, NodeSetFactory factory) {
        this.graph = graph;
        this.grayNodes = factory.create(GRAY_NODES_KEY);
    }

    public List<? extends IncrementalTask> getCollectorStagesToExecute() {
        return Arrays.asList(
                new MarkRootNodes(graph.getRootNodes().iterator()),
                new ScanMarkedNodes(),
                new RemoveGarbageNodesAndDoCleanup(graph.getAllNodes().iterator()));
    }

    public MutatorListener<T> getMutatorListener() {
        return new MutatorListener<T>() {

            public void onNodeCreated(T node) {
//                System.out.println("onNodeCreated " + node);
//                setColor(node, Color.BLACK);
            }

            public void onReferenceCreated(@Nullable T source, T target) {
//                System.out.println("onReferenceCreated " + source + " -> " + target);
                if (getColor(target).equals(Color.WHITE)) {
                    setColor(target, Color.GRAY);
                }
            }

            public void onReferenceRemoved(@Nullable T source, T target) {
//                System.out.println("onReferenceRemoved " + source + " -> " + target);
            }
        };
    }

    public Color getColor(T node) {
        byte[] value = graph.getMetadata(node, COLOR_KEY);
        if (value.length == 0) {
            // TODO: Revert the default back to white and scan new nodes black in MutatorListener.onNodeCreated(),
            // but there is a problem that bindings to an entity may be created before the entity is persisted,
            // which results in not being able to modify its metadata.
            return Color.BLACK;
//            return Color.WHITE;
        }
        return Color.parseIndex(value[0]);
    }

    private void setColor(T node, Color color) {
        if (color.equals(Color.GRAY)) {
            grayNodes.add(node);
        }
        byte[] value = {(byte) color.getIndex()};
        graph.setMetadata(node, COLOR_KEY, value);
    }


    private class MarkRootNodes implements IncrementalTask, Serializable {
        private static final long serialVersionUID = 1L;

        private final Iterator<T> rootNodes;

        public MarkRootNodes(Iterator<T> rootNodes) {
            this.rootNodes = rootNodes;
        }

        public Collection<? extends IncrementalTask> step() {
            if (!rootNodes.hasNext()) {
                return Collections.emptyList();
            }
            T current = rootNodes.next();
            setColor(current, Color.GRAY);
            return Arrays.asList(new MarkRootNodes(rootNodes));
        }
    }

    private class ScanMarkedNodes implements IncrementalTask, Serializable {
        private static final long serialVersionUID = 1L;

        public Collection<? extends IncrementalTask> step() {
            T current = grayNodes.pollFirst();
            if (current == null) {
                return Collections.emptyList();
            }
            if (isNotScanned(current)) {
                scanNode(current);
            }
            return Arrays.asList(new ScanMarkedNodes());
        }

        private boolean isNotScanned(T node) {
            return !getColor(node).equals(Color.BLACK);
        }

        private void scanNode(T node) {
            setColor(node, Color.BLACK);
            markUnseenChildren(node);
        }

        private void markUnseenChildren(T node) {
            for (T child : graph.getConnectedNodesOf(node)) {
                if (isNotSeen(child)) {
                    setColor(child, Color.GRAY);
                }
            }
        }

        private boolean isNotSeen(T node) {
            return getColor(node).equals(Color.WHITE);
        }
    }

    private class RemoveGarbageNodesAndDoCleanup implements IncrementalTask, Serializable {
        private static final long serialVersionUID = 1L;

        private final Iterator<T> allNodes;

        public RemoveGarbageNodesAndDoCleanup(Iterator<T> allNodes) {
            this.allNodes = allNodes;
        }

        public Collection<? extends IncrementalTask> step() {
            if (!allNodes.hasNext()) {
                return Collections.emptyList();
            }
            T current = allNodes.next();
            if (isGarbage(current)) {
                graph.removeNode(current);
            } else {
                clearNodeColor(current);
            }
            return Arrays.asList(new RemoveGarbageNodesAndDoCleanup(allNodes));
        }

        private boolean isGarbage(T node) {
            return getColor(node).equals(Color.WHITE);
        }

        private void clearNodeColor(T node) {
            setColor(node, Color.WHITE);
        }
    }
}
