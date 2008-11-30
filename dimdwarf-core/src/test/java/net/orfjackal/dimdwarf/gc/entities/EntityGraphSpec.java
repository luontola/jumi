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
import org.junit.runner.RunWith;

import java.math.BigInteger;

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
    private Provider<BindingStorage> bindings;

    public void create() throws Exception {
        Injector injector = Guice.createInjector(
                new EntityModule(),
                new DatabaseModule(),
                new TaskContextModule()
        );
        taskContext = injector.getInstance(TaskExecutor.class);
        info = injector.getProvider(EntityInfo.class);
        bindings = injector.getProvider(BindingStorage.class);

        graph = injector.getProvider(EntityGraph.class);
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

    public class WhenAnEntityIsCreated {

        private BigInteger entityId;

        public void create() {
            taskContext.execute(new Runnable() {
                public void run() {
                    DummyEntity e = new DummyEntity();
                    entityId = info.get().getEntityId(e);
                }
            });
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
    }

    public class WhenABindingIsCreated {

        private BigInteger entityId;

        public void create() {
            taskContext.execute(new Runnable() {
                public void run() {
                    DummyEntity e = new DummyEntity();
                    entityId = info.get().getEntityId(e);
                    bindings.get().update("binding", e);
                }
            });
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
}
