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

package net.orfjackal.dimdwarf.gc.cms;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.gc.*;

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
                // TODO: scan black?
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
            // TODO: Revert the default back to white? Maybe also scan new nodes black in MutatorListener.onNodeCreated.
            return Color.BLACK;
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
