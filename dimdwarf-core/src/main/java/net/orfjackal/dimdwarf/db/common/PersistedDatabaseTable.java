// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

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
