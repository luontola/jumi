// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db;

import javax.annotation.Nullable;

/**
 * @author Esko Luontola
 * @since 2.12.2008
 */
public interface DatabaseTableWithMetadata<K, V> extends DatabaseTable<K, V> {

    String META_SEPARATOR = "$";

    V readMetadata(K key, String metaKey);

    void updateMetadata(K key, String metaKey, V metaValue);

    void deleteMetadata(K key, String metaKey);

    @Nullable
    K firstEntryWithMetadata(String metaKey);
}
