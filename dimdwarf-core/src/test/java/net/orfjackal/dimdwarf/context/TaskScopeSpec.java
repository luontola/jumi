// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.context;

import com.google.inject.*;
import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.modules.*;
import net.orfjackal.dimdwarf.tasks.*;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(JDaveRunner.class)
@Group({"fast"})
public class TaskScopeSpec extends Specification<Object> {

    // XXX: we are not actually testing TaskScope, but ThreadScope - refactor this class to reflect it, maybe split it

    private Injector injector;
    private Provider<TaskContext> contextProvider;

    public void create() throws Exception {
        injector = Guice.createInjector(
                new MyModule(),
                new TaskContextModule(),
                new FakeEntityModule(this)
        );
        contextProvider = injector.getProvider(TaskContext.class);
    }

    public void destroy() throws Exception {
        if (ThreadContext.getCurrentContext() != null) {
            ThreadContext.tearDown();
        }
    }

    private static void awaitForOthers(CountDownLatch latch) {
        try {
            latch.countDown();
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public class WhenOutOfTaskScope {

        public void taskScopedBindingsCanNotBeAccessed() {
            specify(new Block() {
                public void run() throws Throwable {
                    injector.getInstance(MyService.class);
                }
            }, should.raise(ProvisionException.class));
        }

        public void taskScopedBindingsCanNotBeAccessedInOtherThreadContexts() {
            ThreadContext.setUp(new FakeContext());
            specify(new Block() {
                public void run() throws Throwable {
                    injector.getInstance(MyService.class);
                }
            }, should.raise(ProvisionException.class));
        }
    }

    public class WhenInsideTaskScope {

        public void create() {
            ThreadContext.setUp(contextProvider.get());
        }

        public void taskScopedBindingsMayBeAccessed() {
            specify(injector.getInstance(MyService.class), should.not().equal(null));
        }

        public void onlyOneInstanceIsCreatedPerBinding() {
            MyService s1 = injector.getInstance(MyService.class);
            MyService s2 = injector.getInstance(MyService.class);
            specify(s1, should.equal(s2));
        }

        public void eachNewScopeHasItsOwnInstances() {
            MyService s1 = injector.getInstance(MyService.class);
            ThreadContext.tearDown();
            ThreadContext.setUp(contextProvider.get());
            MyService s2 = injector.getInstance(MyService.class);
            specify(s1, should.not().equal(s2));
        }

        public void eachConcurrentScopeHasItsOwnInstances() throws InterruptedException {
            final AtomicReference<MyService> s1 = new AtomicReference<MyService>();
            final AtomicReference<MyService> s2 = new AtomicReference<MyService>();
            final CountDownLatch taskRunning = new CountDownLatch(2);
            final CountDownLatch gotInstance = new CountDownLatch(2);

            Thread t1 = new Thread(new Runnable() {
                public void run() {
                    ThreadContext.runInContext(contextProvider.get(), new Runnable() {
                        public void run() {
                            awaitForOthers(taskRunning);
                            s1.set(injector.getInstance(MyService.class));
                            awaitForOthers(gotInstance);
                        }
                    });
                }
            });
            Thread t2 = new Thread(new Runnable() {
                public void run() {
                    ThreadContext.runInContext(contextProvider.get(), new Runnable() {
                        public void run() {
                            awaitForOthers(taskRunning);
                            s2.set(injector.getInstance(MyService.class));
                            awaitForOthers(gotInstance);
                        }
                    });
                }
            });
            t1.setDaemon(true);
            t1.start();
            t2.setDaemon(true);
            t2.start();
            gotInstance.await();

            specify(s1.get(), should.not().equal(null));
            specify(s2.get(), should.not().equal(null));
            specify(s1.get(), should.not().equal(s2.get()));
        }

        public void boundInstancesCanBeAccessedThroughThreadContext() {
            specify(ThreadContext.get(MyService.class), should.not().equal(null));
        }
    }


    public static class MyModule extends AbstractModule {
        protected void configure() {
            bind(MyService.class)
                    .to(MyServiceImpl.class);
        }
    }

    public interface MyService {
    }

    @TaskScoped
    public static class MyServiceImpl implements MyService {
    }
}
