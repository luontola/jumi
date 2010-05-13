// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db.common;

import net.orfjackal.dimdwarf.db.Blob;

import javax.annotation.*;
import java.util.Map;

public interface PersistedDatabaseTable<H> {

    @Nullable
    Blob firstKey(H handle);

    @Nullable
    Blob nextKeyAfter(Blob currentKey, H handle);

    @Nullable
    Blob get(Blob key, H handle);

    @CheckReturnValue
    CommitHandle prepare(Map<Blob, Blob> updates, H handle);
}
