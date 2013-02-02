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

    private final SuiteFactory factory = new SuiteFactory(new DaemonConfiguration(), new OutputCapturer(), new PrintStream(new NullOutputStream()));

    @After
    public void tearDown() {
        factory.close();
    }

    @Test(timeout = TIMEOUT)
    public void sets_the_context_class_loader_for_test_threads() throws InterruptedException {
        factory.configure(new SuiteConfiguration());
        factory.start(new NullSuiteListener());
        final BlockingQueue<ClassLoader> spy = new LinkedBlockingQueue<>();

        factory.testExecutor.execute(new Runnable() {
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
        final BlockingQueue<String> spy = new LinkedBlockingQueue<>();
        factory.configure(new SuiteConfiguration());
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
    public void reports_uncaught_exceptions_from_test_threads_as_internal_errors() throws InterruptedException {
        final BlockingQueue<String> spy = new LinkedBlockingQueue<>();
        factory.configure(new SuiteConfiguration());
        factory.start(new NullSuiteListener() {
            @Override
            public void onInternalError(String message, StackTrace cause) {
                spy.add(message);
            }
        });

        factory.testExecutor.execute(new Runnable() {
            @Override
            public void run() {
                throw new RuntimeException("dummy exception");
            }
        });

        assertThat(spy.take(), startsWith("Uncaught exception in thread jumi-test-"));
    }
}
