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

package net.orfjackal.dimdwarf.gc;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author Esko Luontola
 * @since 29.11.2008
 */
@ThreadSafe
public class MockGraph implements Graph<String> {

    private final Set<String> nodes = new CopyOnWriteArraySet<String>();
    private final Set<String> roots = new CopyOnWriteArraySet<String>();
    private final Map<String, List<String>> edges = new ConcurrentHashMap<String, List<String>>();

    public Iterable<String> getAllNodes() {
        return Collections.unmodifiableCollection(nodes);
    }

    public Iterable<String> getRootNodes() {
        return Collections.unmodifiableCollection(roots);
    }

    public Iterable<String> getConnectedNodesOf(String node) {
        return Collections.unmodifiableCollection(connectedNodesOf(node));
    }

    public synchronized void createNode(String node) {
        nodes.add(node);
    }

    public synchronized void createRootNode(String node) {
        nodes.add(node);
        roots.add(node);
    }

    public synchronized void removeNode(String node) {
        nodes.remove(node);
        roots.remove(node);
        edges.remove(node);
    }

    public void createDirectedEdge(String from, String to) {
        connectedNodesOf(from).add(to);
    }

    public void removeDirectedEdge(String from, String to) {
        connectedNodesOf(from).remove(to);
    }

    private synchronized List<String> connectedNodesOf(String node) {
        List<String> connected = edges.get(node);
        if (connected == null) {
            connected = new ArrayList<String>();
            edges.put(node, connected);
        }
        return connected;
    }
}
