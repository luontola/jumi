// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.gc.entities;

import com.google.inject.*;
import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.*;
import net.orfjackal.dimdwarf.entities.*;
import net.orfjackal.dimdwarf.modules.CommonModules;
import net.orfjackal.dimdwarf.modules.options.NullGarbageCollectionOption;
import net.orfjackal.dimdwarf.tasks.TaskExecutor;
import net.orfjackal.dimdwarf.util.Objects;
import org.junit.runner.RunWith;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.*;

/**
 * @author Esko Luontola
 * @since 30.11.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class EntityGraphSpec extends Specification<Object> {

    private Executor taskContext;
    private Provider<EntityInfo> info;
    private Provider<BindingRepository> bindings;
    private Provider<EntityGraph> graph;

    private EntityId entityId;

    public void create() throws Exception {
        Injector injector = Guice.createInjector(
                new CommonModules(),
                new NullGarbageCollectionOption()
        );
        taskContext = injector.getInstance(TaskExecutor.class);
        info = injector.getProvider(EntityInfo.class);
        bindings = injector.getProvider(BindingRepository.class);

        graph = injector.getProvider(EntityGraph.class);
    }

    private EntityId createDummyEntity() {
        final AtomicReference<EntityId> entityId = new AtomicReference<EntityId>();
        taskContext.execute(new Runnable() {
            public void run() {
                DummyEntity e = new DummyEntity();
                entityId.set(info.get().getEntityId(e));
            }
        });
        return entityId.get();
    }

    private EntityId createBoundDummyEntity(final String binding) {
        final AtomicReference<EntityId> entityId = new AtomicReference<EntityId>();
        taskContext.execute(new Runnable() {
            public void run() {
                DummyEntity e = new DummyEntity();
                entityId.set(info.get().getEntityId(e));
                bindings.get().update(binding, e);
            }
        });
        return entityId.get();
    }


    public class WhenThereAreNoEntities {

        public void thereAreNoNodes() {
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(graph.get().getAllNodes(), should.containExactly());
                }
            });
        }

        public void thereAreNoRootNodes() {
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(graph.get().getRootNodes(), should.containExactly());
                }
            });
        }
    }

    public class WhenThereIsAnEntity {

        public void create() {
            entityId = createDummyEntity();
        }

        public void aNodeExists() {
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(graph.get().getAllNodes(), should.containExactly(entityId));
                }
            });
        }

        public void itIsNotARootNode() {
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(graph.get().getRootNodes(), should.containExactly());
                }
            });
        }

        public void itHasNoMetadata() {
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(graph.get().getMetadata(entityId, "metadata"), should.containInOrder(new byte[0]));
                }
            });
        }

        public void itHasNoConnectedNodes() {
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(graph.get().getConnectedNodesOf(entityId), should.containExactly());
                }
            });
        }
    }

    public class WhenThereIsABindingToTheEntity {

        public void create() {
            entityId = createBoundDummyEntity("binding");
        }

        public void aNodeExists() {
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(graph.get().getAllNodes(), should.containExactly(entityId));
                }
            });
        }

        public void itIsARootNode() {
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(graph.get().getRootNodes(), should.containExactly(entityId));
                }
            });
        }
    }

    public class WhenThereAreManyEntities {

        private EntityId entityId1;
        private EntityId entityId2;
        private EntityId entityId3;

        public void create() {
            entityId1 = createBoundDummyEntity("binding1");
            entityId2 = createBoundDummyEntity("binding2");
            entityId3 = createDummyEntity();
        }

        public void iteratingAllNodesCanBeDividedToTasks() {
            taskContext.execute(new Runnable() {
                public void run() {
                    setIterator(graph.get().getAllNodes().iterator());
                }
            });
            specify(iterateInSeparateTasks(), should.containExactly(entityId1, entityId2, entityId3));
        }

        public void iteratingRootNodesCanBeDividedToTasks() {
            taskContext.execute(new Runnable() {
                public void run() {
                    setIterator(graph.get().getRootNodes().iterator());
                }
            });
            specify(iterateInSeparateTasks(), should.containExactly(entityId1, entityId2));
        }

        private List<EntityId> iterateInSeparateTasks() {
            final AtomicBoolean hasNext = new AtomicBoolean();
            final List<EntityId> nodes = new ArrayList<EntityId>();
            do {
                taskContext.execute(new Runnable() {
                    public void run() {
                        Iterator<EntityId> it = getIterator();
                        hasNext.set(it.hasNext());
                        if (it.hasNext()) {
                            nodes.add(it.next());
                        }
                    }
                });
            } while (hasNext.get());
            return nodes;
        }

        private Iterator<EntityId> getIterator() {
            return Objects.uncheckedCast(getHolder().getOther());
        }

        private void setIterator(Iterator<EntityId> iter) {
            getHolder().setOther(iter);
        }

        private DummyEntity getHolder() {
            return (DummyEntity) bindings.get().read("binding1");
        }
    }

    public class WhenTheEntityHasReferencesToOtherEntities {

        private EntityId entityId1;
        private EntityId entityId2;

        public void create() {
            taskContext.execute(new Runnable() {
                public void run() {
                    DummyEntity e1 = new DummyEntity();
                    DummyEntity e2 = new DummyEntity();
                    e1.setOther(e2);
                    entityId1 = info.get().getEntityId(e1);
                    entityId2 = info.get().getEntityId(e2);
                }
            });
        }

        public void thatNodeIsConnectedToTheOtherNodes() {
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(graph.get().getConnectedNodesOf(entityId1), should.containExactly(entityId2));
                }
            });
        }
    }

    public class WhenANodeIsRemovedFromTheGraph {

        public void create() {
            entityId = createDummyEntity();
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(graph.get().getAllNodes(), should.containExactly(entityId));
                    graph.get().removeNode(entityId);
                }
            });
        }

        public void theNodeDoesNotExistAnymore() {
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(graph.get().getAllNodes(), should.containExactly());
                }
            });
        }
    }

    public class WhenANodeHasSomeMetadata {

        public void create() {
            entityId = createDummyEntity();
            taskContext.execute(new Runnable() {
                public void run() {
                    graph.get().setMetadata(entityId, "metadata", new byte[]{0x01});
                }
            });
        }

        public void theNodeHasThatMetadata() {
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(graph.get().getMetadata(entityId, "metadata"), should.containInOrder(new byte[]{0x01}));
                }
            });
        }
    }
}
