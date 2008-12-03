/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 *     * Neither the name of the copyright holder nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.orfjackal.dimdwarf.gc.entities;

import com.google.inject.*;
import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.EntityInfo;
import net.orfjackal.dimdwarf.entities.*;
import net.orfjackal.dimdwarf.modules.*;
import net.orfjackal.dimdwarf.tasks.TaskExecutor;
import net.orfjackal.dimdwarf.util.Objects;
import org.junit.runner.RunWith;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.*;

/**
 * @author Esko Luontola
 * @since 30.11.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class EntityGraphSpec extends Specification<Object> {

    private Provider<EntityGraph> graph;

    private TaskExecutor taskContext;
    private Provider<EntityInfo> info;
    private Provider<BindingRepository> bindings;

    private BigInteger entityId;

    public void create() throws Exception {
        Injector injector = Guice.createInjector(
                new EntityModule(),
                new DatabaseModule(),
                new TaskContextModule()
        );
        taskContext = injector.getInstance(TaskExecutor.class);
        info = injector.getProvider(EntityInfo.class);
        bindings = injector.getProvider(BindingRepository.class);

        graph = injector.getProvider(EntityGraph.class);
    }

    private BigInteger createDummyEntity() {
        final AtomicReference<BigInteger> entityId = new AtomicReference<BigInteger>();
        taskContext.execute(new Runnable() {
            public void run() {
                DummyEntity e = new DummyEntity();
                entityId.set(info.get().getEntityId(e));
            }
        });
        return entityId.get();
    }

    private BigInteger createBoundDummyEntity(final String binding) {
        final AtomicReference<BigInteger> entityId = new AtomicReference<BigInteger>();
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

        public void itHasDefaultStatus() {
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(graph.get().getStatus(entityId), should.equal(0L));
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

        private BigInteger entityId1;
        private BigInteger entityId2;
        private BigInteger entityId3;

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

        private List<BigInteger> iterateInSeparateTasks() {
            final AtomicBoolean hasNext = new AtomicBoolean();
            final List<BigInteger> nodes = new ArrayList<BigInteger>();
            do {
                taskContext.execute(new Runnable() {
                    public void run() {
                        Iterator<BigInteger> it = getIterator();
                        hasNext.set(it.hasNext());
                        if (it.hasNext()) {
                            nodes.add(it.next());
                        }
                    }
                });
            } while (hasNext.get());
            return nodes;
        }

        private Iterator<BigInteger> getIterator() {
            return Objects.uncheckedCast(getHolder().getOther());
        }

        private void setIterator(Iterator<BigInteger> iter) {
            getHolder().setOther(iter);
        }

        private DummyEntity getHolder() {
            return (DummyEntity) bindings.get().read("binding1");
        }
    }

    public class WhenTheEntityHasReferencesToOtherEntities {

        private BigInteger entityId1;
        private BigInteger entityId2;

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

    public class WhenTheStatusOfANodeIsSet {

        public void create() {
            entityId = createDummyEntity();
            taskContext.execute(new Runnable() {
                public void run() {
                    graph.get().setStatus(entityId, 1L);
                }
            });
        }

        public void theNodeHasThatStatus() {
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(graph.get().getStatus(entityId), should.equal(1L));
                }
            });
        }
    }
}
