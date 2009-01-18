/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008-2009, Esko Luontola. All Rights Reserved.
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
