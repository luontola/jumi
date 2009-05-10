// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.gc.entities;

import com.google.inject.*;
import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.EntityInfo;
import net.orfjackal.dimdwarf.entities.DummyEntity;
import net.orfjackal.dimdwarf.gc.*;
import net.orfjackal.dimdwarf.modules.CommonModules;
import net.orfjackal.dimdwarf.modules.options.NullGarbageCollectionOption;
import net.orfjackal.dimdwarf.tasks.TaskExecutor;
import org.junit.runner.RunWith;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Executor;

/**
 * @author Esko Luontola
 * @since 12.12.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class EntityNodeSetSpec extends Specification<Object> {

    private static final String SET_NAME = "set-name";
    private static final String OTHER_SET_NAME = "other-set-name";

    private Executor taskContext;
    private Provider<EntityInfo> info;
    private Provider<NodeSetFactory> factory;

    private BigInteger entityId1;
    private BigInteger entityId2;

    public void create() throws Exception {
        Injector injector = Guice.createInjector(
                new CommonModules(),
                new NullGarbageCollectionOption()
        );
        taskContext = injector.getInstance(TaskExecutor.class);
        info = injector.getProvider(EntityInfo.class);

        factory = injector.getProvider(NodeSetFactory.class);
    }

    private NodeSet<BigInteger> getNodeSet(String name) {
        return factory.get().create(name);
    }


    public class WhenTheSetIsEmpty {

        public void itContainsNoEntityIds() {
            taskContext.execute(new Runnable() {
                public void run() {
                    NodeSet<BigInteger> set = getNodeSet(SET_NAME);
                    specify(set.pollFirst(), should.equal(null));
                }
            });
        }
    }

    public class WhenTheSetContainsEntityIds {

        public void create() {
            taskContext.execute(new Runnable() {
                public void run() {
                    DummyEntity e1 = new DummyEntity("A");
                    DummyEntity e2 = new DummyEntity("A");
                    entityId1 = info.get().getEntityId(e1);
                    entityId2 = info.get().getEntityId(e2);
                }
            });
            taskContext.execute(new Runnable() {
                public void run() {
                    NodeSet<BigInteger> set = getNodeSet(SET_NAME);
                    set.add(entityId1);
                    set.add(entityId2);
                }
            });
        }

        public void thoseEntityIdsCanBeTakenFromIt() {
            taskContext.execute(new Runnable() {
                public void run() {
                    NodeSet<BigInteger> set = getNodeSet(SET_NAME);
                    List<BigInteger> taken = new ArrayList<BigInteger>();
                    taken.add(set.pollFirst());
                    taken.add(set.pollFirst());
                    specify(taken, should.containExactly(entityId1, entityId2));
                    specify(set.pollFirst(), should.equal(null));
                }
            });
        }

        public void otherSetsHaveDifferentContents() {
            taskContext.execute(new Runnable() {
                public void run() {
                    NodeSet<BigInteger> set = getNodeSet(OTHER_SET_NAME);
                    specify(set.pollFirst(), should.equal(null));
                }
            });
        }
    }
}
