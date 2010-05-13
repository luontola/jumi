// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import com.google.inject.*;
import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.modules.*;
import net.orfjackal.dimdwarf.tasks.TaskExecutor;
import org.junit.runner.RunWith;

import java.util.concurrent.Executor;

/**
 * @author Esko Luontola
 * @since 12.9.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class BindingRepositorySpec extends Specification<Object> {

    private static final String BINDING = "binding";
    private static final String INVALID_BINDING = "no-such-binding";

    private Executor taskContext;
    private Provider<BindingRepositoryImpl> bindings;

    public void create() throws Exception {
        Injector injector = Guice.createInjector(
                new TaskContextModule(),
                new DatabaseModule(),
                new EntityModule()
        );
        taskContext = injector.getInstance(TaskExecutor.class);
        bindings = injector.getProvider(BindingRepositoryImpl.class);
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
