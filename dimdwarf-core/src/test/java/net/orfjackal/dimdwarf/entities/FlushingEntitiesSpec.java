// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.internal.DimdwarfEntityApi;
import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.junit.runner.RunWith;

import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 25.8.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class FlushingEntitiesSpec extends Specification<Object> {

    private EntityRepository repository;
    private EntityManagerImpl manager;
    private ReferenceFactory refFactory;
    private DummyEntity entity;
    private DummyEntity newEntity;

    public void create() throws Exception {
        repository = mock(EntityRepository.class);
        manager = new EntityManagerImpl(new EntityIdFactoryImpl(BigInteger.ZERO), repository, new DimdwarfEntityApi());
        refFactory = new ReferenceFactoryImpl(manager);

        entity = new DummyEntity();
        newEntity = new DummyEntity();
        refFactory.createReference(entity);
    }

    public class WhenRegisteredEntitiesAreFlushed {

        public void theyAreStoredInDatabase() {
            checking(new Expectations() {{
                one(repository).update(BigInteger.ONE, entity);
            }});
            manager.flushAllEntitiesToDatabase();
        }

        public void flushingTwiseIsNotAllowed() {
            checking(new Expectations() {{
                one(repository).update(BigInteger.ONE, entity);
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
                one(repository).update(BigInteger.ONE, entity);
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
                    manager.getEntityById(BigInteger.ONE);
                }
            }, should.raise(IllegalStateException.class));
            specify(new Block() {
                public void run() throws Throwable {
                    manager.firstKey();
                }
            }, should.raise(IllegalStateException.class));
            specify(new Block() {
                public void run() throws Throwable {
                    manager.nextKeyAfter(BigInteger.ONE);
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
                one(repository).update(BigInteger.ONE, entity); will(new RegisterEntity(newEntity));
                one(repository).update(BigInteger.valueOf(2), newEntity);
            }});
            manager.flushAllEntitiesToDatabase();
        }
    }

    public class WhenAlreadyRegisteredEntitiesAreRegisteredDuringFlush {

        public void theyAreStoredInDatabaseOnlyOnce() {
            checking(new Expectations() {{
                one(repository).update(BigInteger.ONE, entity); will(new RegisterEntity(entity));
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
