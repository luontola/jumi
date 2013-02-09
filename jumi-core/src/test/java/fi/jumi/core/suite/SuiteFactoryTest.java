// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.suite;

import fi.jumi.actors.*;
import fi.jumi.core.api.*;
import fi.jumi.core.config.*;
import fi.jumi.core.output.OutputCapturer;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.*;

import java.io.PrintStream;
import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SuiteFactoryTest {

    private static final long TIMEOUT = 1000;

    private DaemonConfigurationBuilder daemon = new DaemonConfigurationBuilder();
    private SuiteFactory factory;

    private void createSuiteFactory() {
        factory = new SuiteFactory(daemon.freeze(), new OutputCapturer(), new PrintStream(new NullOutputStream()));
        factory.configure(new SuiteConfiguration());
    }

    @After
    public void tearDown() {
        if (factory != null) {
            factory.close();
        }
    }

    @Test(timeout = TIMEOUT)
    public void sets_the_context_class_loader_for_test_threads() throws InterruptedException {
        createSuiteFactory();

        factory.start(new NullSuiteListener());
        final BlockingQueue<ClassLoader> spy = new LinkedBlockingQueue<>();

        factory.testThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                spy.add(Thread.currentThread().getContextClassLoader());
            }
        });
        ClassLoader contextClassLoader = spy.take();

        assertThat(contextClassLoader, is(factory.testClassLoader));
    }

    @Test(timeout = TIMEOUT)
    public void reports_uncaught_exceptions_from_actors_as_internal_errors() throws InterruptedException {
        createSuiteFactory();

        final BlockingQueue<String> spy = new LinkedBlockingQueue<>();
        factory.start(new NullSuiteListener() {
            @Override
            public void onInternalError(String message, StackTrace cause) {
                spy.add(message);
            }
        });

        ActorThread actorThread = factory.actors.startActorThread();
        ActorRef<Runnable> dummyActor = actorThread.bindActor(Runnable.class, new Runnable() {
            @Override
            public void run() {
                throw new RuntimeException("dummy exception");
            }
        });
        dummyActor.tell().run();

        assertThat(spy.take(), startsWith("Uncaught exception in thread jumi-actor-"));
    }

    @Test(timeout = TIMEOUT)
    public void test_thread_pool_uses_the_specified_number_of_threads() {
        daemon.setTestThreadsCount(3);
        createSuiteFactory();

        assertThat(getMaxThreads(factory.testThreadPool), is(3));
    }

    @Test(timeout = TIMEOUT)
    public void test_thread_pool_uses_as_many_threads_as_CPU_cores_by_default() {
        createSuiteFactory();

        assertThat(getMaxThreads(factory.testThreadPool), is(4));
    }

    // helpers

    private static int getMaxThreads(ExecutorService executorService) {
        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) executorService;
        return threadPool.getMaximumPoolSize();
    }
}
