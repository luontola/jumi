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
import net.orfjackal.dimdwarf.api.internal.EntityReference;
import net.orfjackal.dimdwarf.entities.*;
import net.orfjackal.dimdwarf.gc.*;
import net.orfjackal.dimdwarf.modules.CommonModules;
import net.orfjackal.dimdwarf.modules.options.NullGarbageCollectionOption;
import net.orfjackal.dimdwarf.tasks.TaskExecutor;
import org.junit.runner.RunWith;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Executor;

/**
 * @author Esko Luontola
 * @since 11.12.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class EntityMutatorListenerSpec extends Specification<Object> {

    private Provider<BindingRepository> bindings;
    private Provider<EntityRepository> entities;
    private Provider<EntityInfo> info;
    private Executor taskContext;
    private MutatorListenerSpy listener;

    public void create() throws Exception {
        listener = new MutatorListenerSpy();
        Injector injector = Guice.createInjector(
                new CommonModules(),
                new AbstractModule() {
                    protected void configure() {
                        bind(new TypeLiteral<GarbageCollector<BigInteger>>() {}).toInstance(new NullGarbageCollectionOption.NullGarbageCollector());
                        bind(new TypeLiteral<MutatorListener<BigInteger>>() {}).toInstance(listener);
                    }
                });
        bindings = injector.getProvider(BindingRepository.class);
        entities = injector.getProvider(EntityRepository.class);
        info = injector.getProvider(EntityInfo.class);
        taskContext = injector.getInstance(TaskExecutor.class);
    }

    public class TheMutatorListener {

        private static final String ROOT_BINDING = "root";

        // graph: root -> node1 -> node2
        private BigInteger rootId;
        private BigInteger nodeId1;
        private BigInteger nodeId2;
        private BigInteger otherNodeId;
        private BigInteger garbageId;

        public void create() {
            taskContext.execute(new Runnable() {
                public void run() {
                    DummyInterface root = new DummyEntity();
                    DummyInterface node1 = new DummyEntity();
                    DummyInterface node2 = new DummyEntity();
                    rootId = info.get().getEntityId(root);
                    nodeId1 = info.get().getEntityId(node1);
                    nodeId2 = info.get().getEntityId(node2);
                    bindings.get().update(ROOT_BINDING, root);
                    root.setOther(node1);
                    node1.setOther(node2);
                }
            });
        }

        public void isNotifiedAboutNewBindings() {
            specify(listener.events, should.contain("+null->" + rootId));
        }

        public void isNotifiedAboutRemovedBindings() {
            listener.events.clear();
            taskContext.execute(new Runnable() {
                public void run() {
                    bindings.get().delete(ROOT_BINDING);
                }
            });
            specify(listener.events, should.containInOrder("-null->" + rootId));
        }

        public void isNotifiedAboutChangedBindings() {
            listener.events.clear();
            taskContext.execute(new Runnable() {
                public void run() {
                    DummyInterface root = (DummyInterface) bindings.get().read(ROOT_BINDING);
                    DummyInterface node1 = (DummyInterface) root.getOther();
                    bindings.get().update(ROOT_BINDING, node1);
                }
            });
            specify(listener.events, should.containInOrder("-null->" + rootId, "+null->" + nodeId1));
        }

        public void isNotifiedAboutNewReferences() {
            specify(listener.events, should.contain("+" + rootId + "->" + nodeId1));
        }

        public void isNotifiedAboutRemovedReferences() {
            listener.events.clear();
            taskContext.execute(new Runnable() {
                public void run() {
                    DummyInterface root = (DummyInterface) bindings.get().read(ROOT_BINDING);
                    root.setOther(null);
                }
            });
            specify(listener.events, should.containInOrder("-" + rootId + "->" + nodeId1));
        }

        public void isNotifiedAboutChangedReferences() {
            listener.events.clear();
            taskContext.execute(new Runnable() {
                public void run() {
                    DummyInterface root = (DummyInterface) bindings.get().read(ROOT_BINDING);
                    DummyInterface node1 = (DummyInterface) root.getOther();
                    DummyInterface node2 = (DummyInterface) node1.getOther();
                    root.setOther(node2);
                }
            });
            specify(listener.events, should.containInOrder("-" + rootId + "->" + nodeId1, "+" + rootId + "->" + nodeId2));
        }

        public void isNotifiedAboutEntitiesWhichAreGarbageAlreadyWhenCreated() {
            listener.events.clear();
            taskContext.execute(new Runnable() {
                public void run() {
                    DummyEntity garbage = new DummyEntity();
                    garbageId = info.get().getEntityId(garbage);
                }
            });
            specify(listener.events, should.containInOrder("+" + garbageId + "->" + garbageId, "-" + garbageId + "->" + garbageId));
        }

        public void isNotifiedAboutEntitiesWhichAreRemovedByTheGarbageCollector() {
            listener.events.clear();
            taskContext.execute(new Runnable() {
                public void run() {
                    entities.get().delete(nodeId1);
                }
            });
            specify(listener.events, should.containInOrder("-" + nodeId1 + "->" + nodeId2));
        }

        public void atMostOneReferenceIsCountedBetweenTwoEntities() {
            rootRefersOtherNodeTwise();
            specify(listener.events, listener.numberOfEvents("+" + rootId + "->" + otherNodeId) == 1);

            rootRefersOtherNodeOnce();
            specify(listener.events, should.containInOrder());

            rootDoesNotReferOtherNode();
            specify(listener.events, should.containInOrder("-" + rootId + "->" + otherNodeId));
        }

        private void rootRefersOtherNodeTwise() {
            listener.events.clear();
            taskContext.execute(new Runnable() {
                public void run() {
                    DummyInterface root = (DummyInterface) bindings.get().read(ROOT_BINDING);
                    DummyEntity other = new DummyEntity();
                    otherNodeId = info.get().getEntityId(other);
                    root.setOther(new EntityReference<?>[]{
                            new EntityReferenceImpl<DummyEntity>(otherNodeId, other),
                            new EntityReferenceImpl<DummyEntity>(otherNodeId, other),
                    });
                }
            });
        }

        private void rootRefersOtherNodeOnce() {
            listener.events.clear();
            taskContext.execute(new Runnable() {
                public void run() {
                    DummyInterface root = (DummyInterface) bindings.get().read(ROOT_BINDING);
                    EntityReference<?>[] twoRefs = (EntityReference<?>[]) root.getOther();
                    twoRefs[1] = null;
                }
            });
        }

        private void rootDoesNotReferOtherNode() {
            listener.events.clear();
            taskContext.execute(new Runnable() {
                public void run() {
                    DummyInterface root = (DummyInterface) bindings.get().read(ROOT_BINDING);
                    EntityReference<?>[] oneRef = (EntityReference<?>[]) root.getOther();
                    oneRef[0] = null;
                }
            });
        }
    }


    private static class MutatorListenerSpy implements MutatorListener<BigInteger> {

        public final List<String> events = new ArrayList<String>();

        public void onReferenceCreated(@Nullable BigInteger source, BigInteger target) {
            events.add("+" + source + "->" + target);
        }

        public void onReferenceRemoved(@Nullable BigInteger source, BigInteger target) {
            events.add("-" + source + "->" + target);
        }

        private int numberOfEvents(String event) {
            int count = 0;
            for (String s : events) {
                if (s.equals(event)) {
                    count++;
                }
            }
            return count;
        }
    }
}
