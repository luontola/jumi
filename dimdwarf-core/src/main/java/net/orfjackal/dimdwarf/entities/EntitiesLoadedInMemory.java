// Copyright © 2008-2010, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

public interface EntitiesLoadedInMemory {

    /**
     * Must be called before transaction deactivates, or the changes to entities will not be persisted.
     */
    void flushToDatabase();
}
