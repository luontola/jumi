// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.gc.entities;

import com.google.inject.*;
import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.gc.Graph;

import java.io.Serializable;

/**
 * @author Esko Luontola
 * @since 10.12.2008
 */
public class EntityGraphWrapper implements Graph<EntityId>, Serializable {
    private static final long serialVersionUID = 1L;

    // TODO: create wrappers like this dynamically, to allow constructor injection of scoped and non-persisted objects

    private transient Provider<EntityGraph> graph;

    @Inject
    public void setGraph(Provider<EntityGraph> graph) {
        this.graph = graph;
    }

    // generated delegates

    public Iterable<EntityId> getAllNodes() {
        return graph.get().getAllNodes();
    }

    public Iterable<EntityId> getRootNodes() {
        return graph.get().getRootNodes();
    }

    public Iterable<EntityId> getConnectedNodesOf(EntityId node) {
        return graph.get().getConnectedNodesOf(node);
    }

    public void removeNode(EntityId node) {
        graph.get().removeNode(node);
    }

    public byte[] getMetadata(EntityId node, String metaKey) {
        return graph.get().getMetadata(node, metaKey);
    }

    public void setMetadata(EntityId node, String metaKey, byte[] metaValue) {
        graph.get().setMetadata(node, metaKey, metaValue);
    }
}
