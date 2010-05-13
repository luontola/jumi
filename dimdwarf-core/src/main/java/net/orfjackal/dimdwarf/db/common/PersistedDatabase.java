// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

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
