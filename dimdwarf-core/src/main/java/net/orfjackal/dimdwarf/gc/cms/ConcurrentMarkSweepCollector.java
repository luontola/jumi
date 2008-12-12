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

    private static final int DEFAULT_MAX_NODES_PER_TASK = 10;
    private static final String COLOR_KEY = "cms-color";

    private final Graph<T> graph;
    private final int maxNodesPerTask;

    @Inject
    public ConcurrentMarkSweepCollector(Graph<T> graph) {
        this(graph, DEFAULT_MAX_NODES_PER_TASK);
    }

    public ConcurrentMarkSweepCollector(Graph<T> graph, int maxNodesPerTask) {
        this.graph = graph;
        this.maxNodesPerTask = maxNodesPerTask;
    }

    public List<? extends IncrementalTask> getCollectorStagesToExecute() {
        return Arrays.asList(
                new MarkRootNodesGray(graph.getRootNodes().iterator()),
                new ScanReachableNodesBlack(graph.getRootNodes().iterator()),
                new MultiStepIncrementalTask(
                        new RemoveUnreachableWhiteNodesAndClearOtherNodesWhite(graph.getAllNodes().iterator()),
                        maxNodesPerTask));
    }

    public MutatorListener<T> getMutatorListener() {
        return new MutatorListener<T>() {

            public void onReferenceCreated(@Nullable T source, T target) {
                if (getColor(target).equals(Color.WHITE)) {
                    setColor(target, Color.GRAY);
                }
            }

            public void onReferenceRemoved(@Nullable T source, T target) {
            }
        };
    }

    public Color getColor(T node) {
        byte[] value = graph.getMetadata(node, COLOR_KEY);
        if (value.length == 0) {
            return Color.WHITE;
            // TODO: introduce 'undefined' - equals black or white depending on the CMS stage
            // (or is it enough that MutatorListener colors nodes which are created during GC?)
        }
        return Color.parseIndex(value[0]);
    }

    private void setColor(T node, Color color) {
        byte[] value = {(byte) color.getIndex()};
        graph.setMetadata(node, COLOR_KEY, value);
    }


    private class MarkRootNodesGray implements IncrementalTask, Serializable {
        private static final long serialVersionUID = 1L;

        private final Iterator<T> rootNodes;

        public MarkRootNodesGray(Iterator<T> rootNodes) {
            this.rootNodes = rootNodes;
        }

        public Collection<? extends IncrementalTask> step() {
            if (!rootNodes.hasNext()) {
                return Collections.emptyList();
            }
            T current = rootNodes.next();
            setColor(current, Color.GRAY);
            return Arrays.asList(
                    new MarkRootNodesGray(rootNodes)
            );
        }
    }

    private class ScanReachableNodesBlack implements IncrementalTask, Serializable {
        private static final long serialVersionUID = 1L;

        private final Iterator<T> nodes;

        public ScanReachableNodesBlack(Iterator<T> nodes) {
            this.nodes = nodes;
        }

        public Collection<? extends IncrementalTask> step() {
            if (!nodes.hasNext()) {
                return Collections.emptyList();
            }
            T current = nodes.next();
            if (getColor(current).equals(Color.BLACK)) {
                return alreadyScanned();
            } else {
                return scanNodeBlack(current);
            }
        }

        private Collection<? extends IncrementalTask> alreadyScanned() {
            return Arrays.asList(
                    new ScanReachableNodesBlack(nodes)
            );
        }

        private Collection<? extends IncrementalTask> scanNodeBlack(T current) {
            setColor(current, Color.BLACK);
            markUnseenConnectedNodesGray(current);
            return Arrays.asList(
                    new ScanReachableNodesBlack(nodes),
                    new ScanReachableNodesBlack(graph.getConnectedNodesOf(current).iterator())
            );
        }

        private void markUnseenConnectedNodesGray(T current) {
            for (T connected : graph.getConnectedNodesOf(current)) {
                if (getColor(connected).equals(Color.WHITE)) {
                    setColor(connected, Color.GRAY);
                }
            }
        }
    }

    private class RemoveUnreachableWhiteNodesAndClearOtherNodesWhite implements IncrementalTask, Serializable {
        private static final long serialVersionUID = 1L;

        private final Iterator<T> nodes;

        public RemoveUnreachableWhiteNodesAndClearOtherNodesWhite(Iterator<T> nodes) {
            this.nodes = nodes;
        }

        public Collection<? extends IncrementalTask> step() {
            if (!nodes.hasNext()) {
                return Collections.emptyList();
            }
            T current = nodes.next();
            if (isUnreachableGarbageNode(current)) {
                graph.removeNode(current);
            } else {
                clearColorToWhite(current);
            }
            return Arrays.asList(
                    new RemoveUnreachableWhiteNodesAndClearOtherNodesWhite(nodes)
            );
        }

        private boolean isUnreachableGarbageNode(T current) {
            return getColor(current).equals(Color.WHITE);
        }

        private void clearColorToWhite(T current) {
            setColor(current, Color.WHITE);
        }
    }
}
