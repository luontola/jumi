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

import net.orfjackal.dimdwarf.gc.*;

import java.util.*;

/**
 * @author Esko Luontola
 * @since 29.11.2008
 */
public class ConcurrentMarkSweepCollector<T> implements GarbageCollector<T> {

    private final Graph<T> graph;

    public ConcurrentMarkSweepCollector(Graph<T> graph) {
        this.graph = graph;
    }

    public List<? extends IncrementalTask> collectorStagesToExecute() {
        return Arrays.asList(
                new MarkAllNodesWhite(graph.getAllNodes().iterator()),
                new MarkReachableNodesBlack(graph.getRootNodes().iterator()),
                new RemoveUnreachableWhiteNodes(graph.getAllNodes().iterator())
        );
    }

    public Color getColor(T node) {
        return Color.fromStatus(graph.getStatus(node));
    }

    private void setColor(T node, Color color) {
        graph.setStatus(node, color.toStatus());
    }


    private class MarkAllNodesWhite implements IncrementalTask {
        private final Iterator<T> nodes;

        public MarkAllNodesWhite(Iterator<T> nodes) {
            this.nodes = nodes;
        }

        public Collection<? extends IncrementalTask> step() {
            if (!nodes.hasNext()) {
                return Collections.emptyList();
            }
            T current = nodes.next();
            setColor(current, Color.WHITE);
            return Arrays.asList(
                    new MarkAllNodesWhite(nodes)
            );
        }
    }

    private class MarkReachableNodesBlack implements IncrementalTask {
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

    private class RemoveUnreachableWhiteNodes implements IncrementalTask {
        private final Iterator<T> nodes;

        public RemoveUnreachableWhiteNodes(Iterator<T> nodes) {
            this.nodes = nodes;
        }

        public Collection<? extends IncrementalTask> step() {
            if (!nodes.hasNext()) {
                return Collections.emptyList();
            }
            T current = nodes.next();
            removeNodeIfUnreachable(current);
            return Arrays.asList(
                    new RemoveUnreachableWhiteNodes(nodes)
            );
        }

        private void removeNodeIfUnreachable(T current) {
            if (getColor(current).equals(Color.WHITE)) {
                graph.removeNode(current);
            }
        }
    }
}
