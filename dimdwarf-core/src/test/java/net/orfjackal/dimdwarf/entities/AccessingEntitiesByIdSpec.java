// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.api.internal.*;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

@RunWith(JDaveRunner.class)
@Group({"fast"})
public class AccessingEntitiesByIdSpec extends Specification<Object> {

    private EntitiesPersistedInDatabase database;
    private AllEntities entities;

    private DummyEntity entity = new DummyEntity();
    private EntityId id = new EntityObjectId(1);

    public void create() throws Exception {
        database = mock(EntitiesPersistedInDatabase.class);
        entities = new EntityManager(null, database, new DimdwarfEntityApi());
    }

    private Expectations loadsFromDatabase(final EntityId id, final DummyEntity entity) {
        return new Expectations() {{
            one(database).read(id); will(returnValue(entity));
        }};
    }


    public class WhenThereAreEntitiesInTheDatabase {

        public void entitiesCanBeLoadedById() {
            checking(loadsFromDatabase(id, entity));
            specify(entities.getEntityById(id), should.equal(entity));
        }

        public void entitiesAreRegisteredOnLoadById() {
            checking(loadsFromDatabase(id, entity));
            Object load1 = entities.getEntityById(id);
            Object load2 = entities.getEntityById(id);
            specify(load1 == load2);
        }
    }
}
