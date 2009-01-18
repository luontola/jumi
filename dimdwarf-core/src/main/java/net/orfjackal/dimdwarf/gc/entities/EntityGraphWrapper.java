/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008-2009, Esko Luontola. All Rights Reserved.
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

package net.orfjackal.dimdwarf.gc.entities;

import com.google.inject.*;
import net.orfjackal.dimdwarf.gc.Graph;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 10.12.2008
 */
public class EntityGraphWrapper implements Graph<BigInteger>, Serializable {
    private static final long serialVersionUID = 1L;

    // TODO: create wrappers like this dynamically, to allow constructor injection of scoped and non-persisted objects

    private transient Provider<EntityGraph> graph;

    @Inject
    public void setGraph(Provider<EntityGraph> graph) {
        this.graph = graph;
    }

    // generated delegates

    public Iterable<BigInteger> getAllNodes() {
        return graph.get().getAllNodes();
    }

    public Iterable<BigInteger> getRootNodes() {
        return graph.get().getRootNodes();
    }

    public Iterable<BigInteger> getConnectedNodesOf(BigInteger node) {
        return graph.get().getConnectedNodesOf(node);
    }

    public void removeNode(BigInteger node) {
        graph.get().removeNode(node);
    }

    public byte[] getMetadata(BigInteger node, String metaKey) {
        return graph.get().getMetadata(node, metaKey);
    }

    public void setMetadata(BigInteger node, String metaKey, byte[] metaValue) {
        graph.get().setMetadata(node, metaKey, metaValue);
    }
}
