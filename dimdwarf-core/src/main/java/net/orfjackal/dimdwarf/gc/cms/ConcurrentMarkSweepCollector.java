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

    private static final int MAX_NODES_TO_REMOVE_PER_TASK = 100;
    private static final String COLOR_KEY = "cms-color";

    private final Graph<T> graph;

    @Inject
    public ConcurrentMarkSweepCollector(Graph<T> graph) {
        this.graph = graph;
    }

    public List<? extends IncrementalTask> getCollectorStagesToExecute() {
        return Arrays.asList(
                new MarkAllRootNodesGray(graph.getRootNodes().iterator()),
                new MarkReachableNodesBlack(graph.getRootNodes().iterator()),
                new MultiStepIncrementalTask(
                        new RemoveUnreachableWhiteNodesAndMarkBlackNodesWhite(graph.getAllNodes().iterator()),
                        MAX_NODES_TO_REMOVE_PER_TASK));
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
            return Color.WHITE; // TODO: introduce 'undefined' - equals black or white depending on the CMS stage
        }
        return Color.parseIndex(value[0]);
    }

    private void setColor(T node, Color color) {
        byte[] value = {(byte) color.getIndex()};
        graph.setMetadata(node, COLOR_KEY, value);
    }


    private class MarkAllRootNodesGray implements IncrementalTask, Serializable {
        private static final long serialVersionUID = 1L;

        private final Iterator<T> rootNodes;

        public MarkAllRootNodesGray(Iterator<T> rootNodes) {
            this.rootNodes = rootNodes;
        }

        public Collection<? extends IncrementalTask> step() {
            if (!rootNodes.hasNext()) {
                return Collections.emptyList();
            }
            T current = rootNodes.next();
            setColor(current, Color.GRAY);
            return Arrays.asList(
                    new MarkAllRootNodesGray(rootNodes)
            );
        }
    }

    private class MarkReachableNodesBlack implements IncrementalTask, Serializable {
        private static final long serialVersionUID = 1L;

        private final Iterator<T> nodes;

        public MarkReachableNodesBlack(Iterator<T> nodes) {
            this.nodes = nodes;
        }

        public Collection<? extends IncrementalTask> step() {
            if (!nodes.hasNext()) {
                return Collections.emptyList();
            }
            T current = nodes.next();
            if (getColor(current).equals(Color.BLACK)) {
                return alreadyReachedAndMarkedBlack();
            } else {
                return markReachedNodeBlack(current);
            }
        }

        private Collection<? extends IncrementalTask> alreadyReachedAndMarkedBlack() {
            return Arrays.asList(
                    new MarkReachableNodesBlack(nodes)
            );
        }

        private Collection<? extends IncrementalTask> markReachedNodeBlack(T current) {
            setColor(current, Color.BLACK);
            markUnseenConnectedNodesGray(current);
            return Arrays.asList(
                    new MarkReachableNodesBlack(nodes),
                    new MarkReachableNodesBlack(graph.getConnectedNodesOf(current).iterator())
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

    private class RemoveUnreachableWhiteNodesAndMarkBlackNodesWhite implements IncrementalTask, Serializable {
        private static final long serialVersionUID = 1L;

        private final Iterator<T> nodes;

        public RemoveUnreachableWhiteNodesAndMarkBlackNodesWhite(Iterator<T> nodes) {
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
                resetColorToWhite(current);
            }
            return Arrays.asList(
                    new RemoveUnreachableWhiteNodesAndMarkBlackNodesWhite(nodes)
            );
        }

        private boolean isUnreachableGarbageNode(T current) {
            return getColor(current).equals(Color.WHITE);
        }

        private void resetColorToWhite(T current) {
            setColor(current, Color.WHITE);
        }
    }

    // TODO: The tasks need to be static and serializable. Integration tests are needed.
}
