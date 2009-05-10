// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.context;

import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.entities.DummyInterface;
import org.junit.runner.RunWith;

/**
 * @author Esko Luontola
 * @since 5.9.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class ThreadContextSpec extends Specification<Object> {

    private Context myContext;
    private MyServiceImpl myService;

    public void create() throws Exception {
        myService = new MyServiceImpl();
        myContext = new FakeContext().with(MyService.class, myService);
    }

    public void destroy() throws Exception {
        if (ThreadContext.getCurrentContext() != null) {
            ThreadContext.tearDown();
        }
    }

    private boolean contextIsDisabled() {
        specify(ThreadContext.getCurrentContext(), should.equal(null));
        specify(new Block() {
            public void run() throws Throwable {
                ThreadContext.get(MyService.class);
            }
        }, should.raise(IllegalStateException.class));
        return true;
    }


    public class AThreadContext {

        private boolean wasExecuted;

        public void givesAccessToTheInstalledServices() {
            ThreadContext.setUp(myContext);
            specify(ThreadContext.getCurrentContext(), should.equal(myContext));
            specify(ThreadContext.get(MyService.class), should.equal(myService));
        }

        public void failsWhenTryingToAccessUnavailableServices() {
            ThreadContext.setUp(myContext);
            specify(new Block() {
                public void run() throws Throwable {
                    ThreadContext.get(DummyInterface.class);
                }
            }, should.raise(IllegalArgumentException.class));
        }

        public void isDisabledBeforeSetup() {
            specify(contextIsDisabled());
        }

        public void isDiabledAfterTearingDown() {
            ThreadContext.setUp(myContext);
            ThreadContext.tearDown();
            specify(contextIsDisabled());
        }

        public void canNotBeSetUpTwise() {
            ThreadContext.setUp(myContext);
            specify(new Block() {
                public void run() throws Throwable {
                    ThreadContext.setUp(new FakeContext());
                }
            }, should.raise(IllegalStateException.class));
            specify(ThreadContext.get(MyService.class), should.equal(myService));
        }

        public void canNotBeTornDownTwise() {
            ThreadContext.setUp(myContext);
            ThreadContext.tearDown();
            specify(new Block() {
                public void run() throws Throwable {
                    ThreadContext.tearDown();
                }
            }, should.raise(IllegalStateException.class));
            specify(contextIsDisabled());
        }

        public void isSpecificToTheCurrentThread() throws InterruptedException {
            Thread t = new Thread(new Runnable() {
                public void run() {
                    ThreadContext.setUp(myContext);
                }
            });
            t.start();
            t.join();
            specify(contextIsDisabled());
        }


        public void providesAHelperMethodWithAutomaticSetUpAndTearDown() {
            wasExecuted = false;
            ThreadContext.runInContext(myContext, new Runnable() {
                public void run() {
                    wasExecuted = true;
                    specify(ThreadContext.get(MyService.class), should.equal(myService));
                }
            });
            specify(wasExecuted);
            specify(contextIsDisabled());
        }

        public void theHelperMethodDoesTearDownEvenWhenExceptionsAreThrown() {
            final Runnable maliciousTask = new Runnable() {
                public void run() {
                    throw new InternalError("dummy exception");
                }
            };
            specify(new Block() {
                public void run() throws Throwable {
                    ThreadContext.runInContext(myContext, maliciousTask);
                }
            }, should.raise(InternalError.class, "dummy exception"));
            specify(contextIsDisabled());
        }
    }


    public interface MyService {
    }

    public static class MyServiceImpl implements MyService {
    }
}
