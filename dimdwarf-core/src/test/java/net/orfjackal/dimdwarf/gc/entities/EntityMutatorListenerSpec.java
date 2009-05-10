// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

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

        public void isNotifiedAboutCreatedEntitiesEvenIfThereAreNoReferencesToThem() {
            listener.events.clear();
            taskContext.execute(new Runnable() {
                public void run() {
                    DummyEntity garbageOnCreation = new DummyEntity();
                    garbageId = info.get().getEntityId(garbageOnCreation);
                }
            });
            specify(listener.events, should.containInOrder("*" + garbageId));
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

        public void onNodeCreated(BigInteger node) {
            events.add("*" + node);
        }

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
