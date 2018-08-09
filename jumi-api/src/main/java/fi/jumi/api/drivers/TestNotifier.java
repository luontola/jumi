// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.api.drivers;

public interface TestNotifier {

    /**
     * Notifies about a test failure.
     * <p>
     * The test will fail if this method is called at least once,<sup>[1]</sup> otherwise the test will
     * pass.<sup>[2]</sup>
     * <p>
     * May be called multiple times.<sup>[3]</sup> Must be called on the current test,<sup>[4][5]</sup> i.e. the latest
     * {@code TestNotifier} which is not yet finished.
     *
     * @reference [1]: <a href="https://github.com/luontola/jumi/blob/b84fa4449300ecb76e37fe8bb682bd053ae9b07c/jumi-core/src/test/java/fi/jumi/core/results/SuiteResultsSummaryTest.java#L31">
     * failing_tests_are_tests_with_failures</a>
     * @reference <br>[2]: <a href="https://github.com/luontola/jumi/blob/b84fa4449300ecb76e37fe8bb682bd053ae9b07c/jumi-core/src/test/java/fi/jumi/core/results/SuiteResultsSummaryTest.java#L22">
     * passing_tests_are_tests_without_failures</a>
     * @reference <br>[3]: <a href="https://github.com/luontola/jumi/blob/b84fa4449300ecb76e37fe8bb682bd053ae9b07c/jumi-core/src/test/java/fi/jumi/core/results/SuiteResultsSummaryTest.java#L41">
     * a_test_with_multiple_failures_counts_as_just_one_failing_test</a>
     * @reference <br>[4]: <a href="https://github.com/luontola/jumi/blob/1eddce9866f4bcc3e9b08b9b447ab6d19f4ec1fc/jumi-core/src/test/java/fi/jumi/core/runs/ThreadBoundSuiteNotifierTest.java#L93">
     * fireFailure_must_be_called_on_the_current_test</a>
     * @reference <br>[5]: <a href="https://github.com/luontola/jumi/blob/1eddce9866f4bcc3e9b08b9b447ab6d19f4ec1fc/jumi-core/src/test/java/fi/jumi/core/runs/ThreadBoundSuiteNotifierTest.java#L115">
     * fireFailure_cannot_be_called_after_the_test_is_finished</a>
     */
    void fireFailure(Throwable cause);

    /**
     * Notifies about the end of a test execution.
     * <p>
     * Must be called last, exactly once.<sup>[1]</sup> Must be called on the current test,<sup>[2]</sup> i.e. the
     * latest {@code TestNotifier} which is not yet finished.
     *
     * @reference [1]: <a href="https://github.com/luontola/jumi/blob/1eddce9866f4bcc3e9b08b9b447ab6d19f4ec1fc/jumi-core/src/test/java/fi/jumi/core/runs/ThreadBoundSuiteNotifierTest.java#L175">
     * fireTestFinished_cannot_be_called_after_the_test_is_finished</a>
     * @reference <br>[2]: <a href="https://github.com/luontola/jumi/blob/1eddce9866f4bcc3e9b08b9b447ab6d19f4ec1fc/jumi-core/src/test/java/fi/jumi/core/runs/ThreadBoundSuiteNotifierTest.java#L153">
     * fireTestFinished_must_be_called_on_the_current_test</a>
     */
    /* TODO: add javadoc after implemented:
       If the test started any threads, will wait for them to finish (except the AWT event thread and maybe some others).
       It is an error (or at least a warning) for a test to start threads without stopping them.
     */
    void fireTestFinished();
}
