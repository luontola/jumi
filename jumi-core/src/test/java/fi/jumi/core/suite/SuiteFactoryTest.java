// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.suite;

import fi.jumi.core.config.*;
import fi.jumi.core.output.OutputCapturer;
import org.junit.*;

import java.util.concurrent.SynchronousQueue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SuiteFactoryTest {

    private final SuiteFactory factory = new SuiteFactory(new DaemonConfiguration(), new OutputCapturer(), System.out);

    @After
    public void tearDown() {
        factory.close();
    }

    @Test
    public void sets_the_context_class_loader_for_test_threads() throws InterruptedException {
        factory.configure(new SuiteConfiguration());
        final SynchronousQueue<ClassLoader> spy = new SynchronousQueue<>();

        factory.testsThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                spy.add(Thread.currentThread().getContextClassLoader());
            }
        });
        ClassLoader contextClassLoader = spy.take();

        assertThat(contextClassLoader, is(factory.testClassLoader));
    }
}
