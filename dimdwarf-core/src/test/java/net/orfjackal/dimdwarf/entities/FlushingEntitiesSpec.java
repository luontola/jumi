// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.api.internal.*;
import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.junit.runner.RunWith;

/**
 * @author Esko Luontola
 * @since 25.8.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class FlushingEntitiesSpec extends Specification<Object> {

    private static final EntityId ID1 = new EntityObjectId(1);
    private static final EntityObjectId ID2 = new EntityObjectId(2);

    private EntityRepository repository;
    private EntityManagerImpl manager;
    private EntityReferenceFactory refFactory;
    private DummyEntity entity;
    private DummyEntity newEntity;

    public void create() throws Exception {
        repository = mock(EntityRepository.class);
        manager = new EntityManagerImpl(new EntityIdFactoryImpl(0), repository, new DimdwarfEntityApi());
        refFactory = new EntityReferenceFactoryImpl(manager);

        entity = new DummyEntity();
        newEntity = new DummyEntity();
        refFactory.createReference(entity);
    }

    public class WhenRegisteredEntitiesAreFlushed {

        public void theyAreStoredInDatabase() {
            checking(new Expectations() {{
                one(repository).update(ID1, entity);
            }});
            manager.flushAllEntitiesToDatabase();
        }

        public void flushingTwiseIsNotAllowed() {
            checking(new Expectations() {{
                one(repository).update(ID1, entity);
            }});
            manager.flushAllEntitiesToDatabase();
            specify(new Block() {
                public void run() throws Throwable {
                    manager.flushAllEntitiesToDatabase();
                }
            }, should.raise(IllegalStateException.class));
        }

        public void theEntityManagerCanNotBeUsedAfterFlushHasEnded() {
            checking(new Expectations() {{
                one(repository).update(ID1, entity);
            }});
            manager.flushAllEntitiesToDatabase();

            // try to call all public methods on the manager - all should fail
            specify(new Block() {
                public void run() throws Throwable {
                    manager.getEntityId(entity);
                }
            }, should.raise(IllegalStateException.class));
            specify(new Block() {
                public void run() throws Throwable {
                    manager.getEntityById(ID1);
                }
            }, should.raise(IllegalStateException.class));
            specify(new Block() {
                public void run() throws Throwable {
                    manager.firstKey();
                }
            }, should.raise(IllegalStateException.class));
            specify(new Block() {
                public void run() throws Throwable {
                    manager.nextKeyAfter(ID1);
                }
            }, should.raise(IllegalStateException.class));
            specify(new Block() {
                public void run() throws Throwable {
                    manager.flushAllEntitiesToDatabase();
                }
            }, should.raise(IllegalStateException.class));
        }
    }

    public class WhenNewEntitiesAreRegisteredDuringFlush {

        public void theyAreStoredInDatabase() {
            checking(new Expectations() {{
                one(repository).update(ID1, entity); will(new RegisterEntity(newEntity));
                one(repository).update(ID2, newEntity);
            }});
            manager.flushAllEntitiesToDatabase();
        }
    }

    public class WhenAlreadyRegisteredEntitiesAreRegisteredDuringFlush {

        public void theyAreStoredInDatabaseOnlyOnce() {
            checking(new Expectations() {{
                one(repository).update(ID1, entity); will(new RegisterEntity(entity));
            }});
            manager.flushAllEntitiesToDatabase();
        }
    }


    private class RegisterEntity extends CustomAction {
        private final DummyEntity entity;

        public RegisterEntity(DummyEntity entity) {
            super("");
            this.entity = entity;
        }

        public Object invoke(Invocation invocation) throws Throwable {
            refFactory.createReference(entity);
            return null;
        }
    }
}
