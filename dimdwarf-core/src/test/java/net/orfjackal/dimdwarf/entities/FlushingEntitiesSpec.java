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

@RunWith(JDaveRunner.class)
@Group({"fast"})
public class FlushingEntitiesSpec extends Specification<Object> {

    private static final EntityId ID1 = new EntityObjectId(1);
    private static final EntityObjectId ID2 = new EntityObjectId(2);

    private EntitiesPersistedInDatabase database;
    private EntityManager manager;
    private EntityReferenceFactory refFactory;
    private DummyEntity entity;
    private DummyEntity newEntity;

    public void create() throws Exception {
        database = mock(EntitiesPersistedInDatabase.class);
        manager = new EntityManager(new EntityIdFactory(0), database, new DimdwarfEntityApi());
        refFactory = new EntityReferenceFactoryImpl(manager);

        entity = new DummyEntity();
        newEntity = new DummyEntity();
        refFactory.createReference(entity);
    }

    public class WhenRegisteredEntitiesAreFlushed {

        public void theyAreStoredInDatabase() {
            checking(new Expectations() {{
                one(database).update(ID1, entity);
            }});
            manager.flushToDatabase();
        }

        public void flushingTwiseIsNotAllowed() {
            checking(new Expectations() {{
                one(database).update(ID1, entity);
            }});
            manager.flushToDatabase();
            specify(new Block() {
                public void run() throws Throwable {
                    manager.flushToDatabase();
                }
            }, should.raise(IllegalStateException.class));
        }

        public void theEntityManagerCanNotBeUsedAfterFlushHasEnded() {
            checking(new Expectations() {{
                one(database).update(ID1, entity);
            }});
            manager.flushToDatabase();

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
                    manager.flushToDatabase();
                }
            }, should.raise(IllegalStateException.class));
        }
    }

    public class WhenNewEntitiesAreRegisteredDuringFlush {

        public void theyAreStoredInDatabase() {
            checking(new Expectations() {{
                one(database).update(ID1, entity); will(registerEntity(newEntity));
                one(database).update(ID2, newEntity);
            }});
            manager.flushToDatabase();
        }
    }

    public class WhenAlreadyRegisteredEntitiesAreRegisteredDuringFlush {

        public void theyAreStoredInDatabaseOnlyOnce() {
            checking(new Expectations() {{
                one(database).update(ID1, entity); will(registerEntity(entity));
            }});
            manager.flushToDatabase();
        }
    }


    private RegisterEntity registerEntity(DummyEntity entity) {
        return new RegisterEntity(entity);
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
