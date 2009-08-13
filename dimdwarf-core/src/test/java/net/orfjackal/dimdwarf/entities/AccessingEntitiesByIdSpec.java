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

    private EntityRepository repository;
    private EntityManager manager;

    private DummyEntity entity1 = new DummyEntity();
    private EntityId id1 = new EntityObjectId(1);
    private EntityId id2 = new EntityObjectId(2);

    public void create() throws Exception {
        repository = mock(EntityRepository.class);
        manager = new EntityManagerImpl(mock(EntityIdFactory.class), repository, new DimdwarfEntityApi());
    }

    private Expectations loadsFromRepository(final EntityId id, final DummyEntity entity) {
        return new Expectations() {{
            one(repository).read(id); will(returnValue(entity));
        }};
    }


    public class WhenThereAreEntitiesInTheDatabase {

        public void entitiesCanBeLoadedById() {
            checking(loadsFromRepository(id1, entity1));
            specify(manager.getEntityById(id1), should.equal(entity1));
        }

        public void entitiesAreRegisteredOnLoadById() {
            checking(loadsFromRepository(id1, entity1));
            Object load1 = manager.getEntityById(id1);
            Object load2 = manager.getEntityById(id1);
            specify(load1 == load2);
        }

        public void entitiesCanBeIteratedById() {
            checking(new Expectations() {{
                one(repository).firstKey(); will(returnValue(id1));
                one(repository).nextKeyAfter(id1); will(returnValue(id2));
            }});
            specify(manager.firstKey(), should.equal(id1));
            specify(manager.nextKeyAfter(id1), should.equal(id2));
        }
    }
}
