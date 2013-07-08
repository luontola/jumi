// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.api.drivers;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.Serializable;
import java.util.concurrent.Executor;

/**
 * Each testing framework should provide its own {@code Driver} implementation so that the Jumi test runner will know
 * how to run tests written using that testing framework.
 */
@NotThreadSafe
public abstract class Driver {

    /**
     * Starts the execution of the tests in {@code testClass}.
     * <p/>
     * The provided {@link Executor} should be used to run the tests, so that they can be executed in parallel, each
     * test in a different thread.<sup>[1]</sup> If the {@link Runnable}s passed to {@code executor} are {@link
     * Serializable}, then each of the tests in one class could potentially be executed on different machine in a server
     * cluster.<sup>[citation needed]</sup> Otherwise any potential clustering is at class-granularity<sup>[citation
     * needed]</sup> (which may be a hindrance for classes with many slow tests).
     *
     * @param testClass contains the tests to be executed.
     * @param notifier  through which Jumi is told about test executions.
     * @param executor  recommended for executing tests asynchronously, instead of running them synchronously in this
     *                  method.
     * @reference [1]: <a href="https://github.com/orfjackal/jumi/blob/1eddce9866f4bcc3e9b08b9b447ab6d19f4ec1fc/end-to-end-tests/src/test/java/fi/jumi/test/RunningTestsTest.java#L103">
     * tests_are_run_in_parallel</a>
     */
    public abstract void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor);
}
