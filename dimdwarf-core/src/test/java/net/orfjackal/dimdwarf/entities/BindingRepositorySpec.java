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

package net.orfjackal.dimdwarf.entities;

import com.google.inject.*;
import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.modules.*;
import net.orfjackal.dimdwarf.modules.options.NullGarbageCollectionOption;
import net.orfjackal.dimdwarf.tasks.TaskExecutor;
import org.junit.runner.RunWith;

/**
 * @author Esko Luontola
 * @since 12.9.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class BindingRepositorySpec extends Specification<Object> {

    private static final String BINDING = "binding";
    private static final String INVALID_BINDING = "no-such-binding";

    private TaskExecutor taskContext;
    private Provider<BindingRepository> bindings;

    public void create() throws Exception {
        Injector injector = Guice.createInjector(
                new TaskContextModule(),
                new DatabaseModule(),
                new EntityModule(),
                new NullGarbageCollectionOption()
        );
        taskContext = injector.getInstance(TaskExecutor.class);
        bindings = injector.getProvider(BindingRepository.class);
    }


    public class ABindingRepository {

        public void create() {
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(bindings.get().exists(BINDING), should.equal(false));
                    bindings.get().update(BINDING, new DummyEntity("A"));
                }
            });
        }

        public void createsBindings() {
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(bindings.get().exists(BINDING));
                }
            });
        }

        public void readsBindings() {
            taskContext.execute(new Runnable() {
                public void run() {
                    DummyInterface e = (DummyInterface) bindings.get().read(BINDING);
                    specify(e.getOther(), should.equal("A"));
                }
            });
        }

        public void updatesBindings() {
            taskContext.execute(new Runnable() {
                public void run() {
                    bindings.get().update(BINDING, new DummyEntity("B"));
                    DummyInterface e = (DummyInterface) bindings.get().read(BINDING);
                    specify(e.getOther(), should.equal("B"));
                }
            });
        }

        public void deletesBindings() {
            taskContext.execute(new Runnable() {
                public void run() {
                    bindings.get().delete(BINDING);
                    specify(bindings.get().exists(BINDING), should.equal(false));
                    specify(bindings.get().read(BINDING), should.equal(null));
                }
            });
        }

        public void canNotReadNonexistentBindings() {
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(bindings.get().exists(INVALID_BINDING), should.equal(false));
                    specify(bindings.get().read(INVALID_BINDING), should.equal(null));
                }
            });
        }
    }

    public class IterationOrderOfBindings {

        public void create() {
            taskContext.execute(new Runnable() {
                public void run() {
                    DummyEntity foo = new DummyEntity();
                    foo.setOther("foo");
                    bindings.get().update("foo", foo);
                    bindings.get().update("foo.2", new DummyEntity());
                    bindings.get().update("foo.1", new DummyEntity());
                    bindings.get().update("bar.x", new DummyEntity());
                    bindings.get().update("bar.y", new DummyEntity());
                }
            });
        }

        public void bindingsAreInAlphabeticalOrder() {
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(bindings.get().firstKey(), should.equal("bar.x"));
                }
            });
        }

        public void whenBindingsHaveTheSamePrefixTheShortestBindingIsFirst() {
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(bindings.get().nextKeyAfter("foo"), should.equal("foo.1"));
                    specify(bindings.get().nextKeyAfter("foo.1"), should.equal("foo.2"));
                    specify(bindings.get().nextKeyAfter("foo.2"), should.equal(null));
                }
            });
        }
    }
}
