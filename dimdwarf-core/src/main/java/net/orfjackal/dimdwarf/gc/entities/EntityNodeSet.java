// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.gc.entities;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.db.Blob;
import net.orfjackal.dimdwarf.entities.dao.EntityDao;
import net.orfjackal.dimdwarf.gc.NodeSet;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 12.12.2008
 */
public class EntityNodeSet implements NodeSet<BigInteger>, Serializable {
    private static final long serialVersionUID = 1L;

    private static final Blob PLACEHOLDER = Blob.fromBytes(new byte[]{1});

    private final String metaKey;
    @Inject public transient final EntityDao entities;

    @Inject
    public EntityNodeSet(String metaKey, EntityDao entities) {
        this.metaKey = metaKey;
        this.entities = entities;
    }

    public void add(BigInteger node) {
        entities.updateMetadata(node, metaKey, PLACEHOLDER);
    }

    public BigInteger pollFirst() {
        BigInteger first = entities.firstEntryWithMetadata(metaKey);
        if (first != null) {
            entities.deleteMetadata(first, metaKey);
        }
        return first;
    }
}
