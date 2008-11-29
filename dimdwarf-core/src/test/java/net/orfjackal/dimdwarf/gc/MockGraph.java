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

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author Esko Luontola
 * @since 29.11.2008
 */
@ThreadSafe
public class MockGraph implements Graph<String> {

    private final Map<String, MockNode> nodes = new ConcurrentHashMap<String, MockNode>();
    private final MockNode root = new MockNode();

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
    }

    public void removeNode(String node) {
        nodes.remove(node);
        root.edges.remove(node);
    }

    public void createDirectedEdge(@Nullable String from, String to) {
        getNode(from).edges.add(to);
    }

    public void removeDirectedEdge(@Nullable String from, String to) {
        getNode(from).edges.remove(to);
    }

    public long getStatus(String node) {
        return getNode(node).status;
    }

    public void setStatus(String node, long status) {
        getNode(node).status = status;
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

    private class MockNode {
        public long status = NULL_STATUS;
        public final List<String> edges = new CopyOnWriteArrayList<String>();
    }
}
