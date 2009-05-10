// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import com.google.inject.*;
import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.modules.*;
import net.orfjackal.dimdwarf.modules.options.NullGarbageCollectionOption;
import net.orfjackal.dimdwarf.tasks.TaskExecutor;
import org.junit.runner.RunWith;

import java.math.BigInteger;
import java.util.concurrent.*;

/**
 * @author Esko Luontola
 * @since 1.9.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class EntityRepositorySpec extends Specification<Object> {

    private static final BigInteger ENTITY_ID = BigInteger.valueOf(42);
    private static final BigInteger INVALID_ENTITY_ID = BigInteger.valueOf(999);

    private Executor taskContext;
    private Provider<EntityRepository> entities;
    private Provider<EntityManager> entityManager;

    public void create() throws Exception {
        Injector injector = Guice.createInjector(
                new TaskContextModule(),
                new DatabaseModule(),
                new EntityModule(),
                new NullGarbageCollectionOption()
        );
        taskContext = injector.getInstance(TaskExecutor.class);
        entities = injector.getProvider(EntityRepository.class);
        entityManager = injector.getProvider(EntityManager.class);
    }

    private void createDummyEntity(final BigInteger entityId, final Object other) {
        taskContext.execute(new Runnable() {
            public void run() {
                specify(entities.get().exists(entityId), should.equal(false));
                entities.get().update(entityId, new DummyEntity(other));
            }
        });
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public class AnEntityRepository {

        public void create() {
            createDummyEntity(ENTITY_ID, "A");
        }

        public void createsEntities() {
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(entities.get().exists(ENTITY_ID));
                }
            });
        }

        public void readsEntities() {
            taskContext.execute(new Runnable() {
                public void run() {
                    DummyInterface e = (DummyInterface) entities.get().read(ENTITY_ID);
                    specify(e.getOther(), should.equal("A"));
                }
            });
        }

        public void updatesEntities() {
            // This is not a realistic test case, because entities are always saved at the end of the task,
            // and they are read using EntityManager. If an ID is already already owned by an entity, the only
            // possible object to be passed to the update() method is the same object which was read earlier
            // with the read() method. Would it be better to remove this test case?
            taskContext.execute(new Runnable() {
                public void run() {
                    entities.get().update(ENTITY_ID, new DummyEntity("B"));
                    DummyInterface e = (DummyInterface) entities.get().read(ENTITY_ID);
                    specify(e.getOther(), should.equal("B"));
                }
            });
        }

        public void deletesEntities() {
            taskContext.execute(new Runnable() {
                public void run() {
                    entities.get().delete(ENTITY_ID);
                    specify(entities.get().exists(ENTITY_ID), should.equal(false));
                    specify(new Block() {
                        public void run() throws Throwable {
                            entities.get().read(ENTITY_ID);
                        }
                    }, should.raise(EntityNotFoundException.class));
                }
            });
        }

        public void canNotReadNonexistentEntities() {
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(entities.get().exists(INVALID_ENTITY_ID), should.equal(false));
                    specify(new Block() {
                        public void run() throws Throwable {
                            entities.get().read(INVALID_ENTITY_ID);
                        }
                    }, should.raise(EntityNotFoundException.class));
                }
            });
        }
    }

    public class WhenAnEntityIsOnlyReadAndNotModified {
        private CountDownLatch bothTasksAreRunning = new CountDownLatch(2);
        private CountDownLatch firstTaskHasFinished = new CountDownLatch(1);

        public void create() {
            createDummyEntity(ENTITY_ID, "A");
        }

        public void theEntityIsNotUpdatedAndThusCanBeReadConcurrentlyInManyTasks() {
            final Runnable task1 = new Runnable() {
                public void run() {
                    bothTasksAreRunning.countDown();
                    await(bothTasksAreRunning);
                    entityManager.get().getEntityById(ENTITY_ID);
                }
            };
            Runnable task2 = new Runnable() {
                public void run() {
                    bothTasksAreRunning.countDown();
                    await(bothTasksAreRunning);
                    entityManager.get().getEntityById(ENTITY_ID);
                    await(firstTaskHasFinished);
                }
            };
            new Thread(new Runnable() {
                public void run() {
                    taskContext.execute(task1);
                    firstTaskHasFinished.countDown();
                }
            }).start();
            // If the entity was updated by the other task, this would throw OptimisticLockException,
            // but we expect that it should not have been updated.
            taskContext.execute(task2);
        }
    }
}
