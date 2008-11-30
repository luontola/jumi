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

package net.orfjackal.dimdwarf.gc.entities;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.api.EntityInfo;
import net.orfjackal.dimdwarf.entities.*;
import net.orfjackal.dimdwarf.gc.Graph;

import java.math.BigInteger;
import java.util.ArrayList;

/**
 * @author Esko Luontola
 * @since 30.11.2008
 */
public class EntityGraph implements Graph<BigInteger> {

    private final EntityStorage entities;
    private final BindingStorage bindings;
    private final EntityInfo info;

    @Inject
    public EntityGraph(EntityStorage entities, BindingStorage bindings, EntityInfo info) {
        this.entities = entities;
        this.bindings = bindings;
        this.info = info;
    }

    public Iterable<BigInteger> getAllNodes() {
        ArrayList<BigInteger> nodes = new ArrayList<BigInteger>();
        for (BigInteger id = entities.firstKey(); id != null; id = entities.nextKeyAfter(id)) {
            nodes.add(id);
        }
        return nodes;
    }

    public Iterable<BigInteger> getRootNodes() {
        ArrayList<BigInteger> nodes = new ArrayList<BigInteger>();
        for (String binding = bindings.firstKey(); binding != null; binding = bindings.nextKeyAfter(binding)) {
            Object entity = bindings.read(binding); // TODO: check for null (need a test)
            BigInteger id = info.getEntityId(entity);
            nodes.add(id);
        }
        return nodes;
    }

    public Iterable<BigInteger> getConnectedNodesOf(BigInteger node) {
        return null;
    }

    public void removeNode(BigInteger node) {
    }

    public long getStatus(BigInteger node) {
        return 0;
    }

    public void setStatus(BigInteger node, long status) {
    }
}
