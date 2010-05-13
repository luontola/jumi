// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db.common;

import net.orfjackal.dimdwarf.db.IsolationLevel;

import javax.annotation.CheckReturnValue;
import java.util.*;

public interface PersistedDatabase<H> {

    IsolationLevel getIsolationLevel();

    Set<String> getTableNames();

    PersistedDatabaseTable<H> openTable(String name);

    @CheckReturnValue
    CommitHandle prepare(Collection<TransientDatabaseTable<H>> updates, H handle);
}
