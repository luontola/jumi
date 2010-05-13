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

/**
 * @author Esko Luontola
 * @since 12.9.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class AccessingEntitiesByIdSpec extends Specification<Object> {

    private EntitiesPersistedInDatabase database;
    private AllEntities entities;

    private DummyEntity entity1 = new DummyEntity();
    private EntityId id1 = new EntityObjectId(1);
    private EntityId id2 = new EntityObjectId(2);

    public void create() throws Exception {
        database = mock(EntitiesPersistedInDatabase.class);
        entities = new EntityManager(mock(EntityIdFactory.class), database, new DimdwarfEntityApi());
    }

    private Expectations loadsFromRepository(final EntityId id, final DummyEntity entity) {
        return new Expectations() {{
            one(database).read(id); will(returnValue(entity));
        }};
    }


    public class WhenThereAreEntitiesInTheDatabase {

        public void entitiesCanBeLoadedById() {
            checking(loadsFromRepository(id1, entity1));
            specify(entities.getEntityById(id1), should.equal(entity1));
        }

        public void entitiesAreRegisteredOnLoadById() {
            checking(loadsFromRepository(id1, entity1));
            Object load1 = entities.getEntityById(id1);
            Object load2 = entities.getEntityById(id1);
            specify(load1 == load2);
        }

        public void entitiesCanBeIteratedById() {
            // TODO: remove the ability to iterate by ID?
            checking(new Expectations() {{
                one(database).firstKey(); will(returnValue(id1));
                one(database).nextKeyAfter(id1); will(returnValue(id2));
            }});
            specify(entities.firstKey(), should.equal(id1));
            specify(entities.nextKeyAfter(id1), should.equal(id2));
        }
    }
}
