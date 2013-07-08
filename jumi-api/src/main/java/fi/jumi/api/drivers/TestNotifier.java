// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.api.drivers;

public interface TestNotifier {

    /**
     * Notifies about a test failure.
     * <p/>
     * The test will fail if this method is called at least once,<sup>[citation needed]</sup> otherwise the test will
     * pass.<sup>[citation needed]</sup>
     * <p/>
     * May be called multiple times.<sup>[citation needed]</sup> Must be called on the current test,<sup>[1][2]</sup>
     * i.e. the latest {@code TestNotifier} which is not yet finished.
     *
     * @reference [1]: <a href="https://github.com/orfjackal/jumi/blob/1eddce9866f4bcc3e9b08b9b447ab6d19f4ec1fc/jumi-core/src/test/java/fi/jumi/core/runs/ThreadBoundSuiteNotifierTest.java#L93">
     * fireFailure_must_be_called_on_the_current_test</a>
     * @reference <br>[2]: <a href="https://github.com/orfjackal/jumi/blob/1eddce9866f4bcc3e9b08b9b447ab6d19f4ec1fc/jumi-core/src/test/java/fi/jumi/core/runs/ThreadBoundSuiteNotifierTest.java#L115">
     * fireFailure_cannot_be_called_after_the_test_is_finished</a>
     */
    void fireFailure(Throwable cause);

    /**
     * Notifies about the end of a test execution.
     * <p/>
     * Must be called last, exactly once.<sup>[1]</sup> Must be called on the current test,<sup>[2]</sup> i.e. the
     * latest {@code TestNotifier} which is not yet finished.
     *
     * @reference [1]: <a href="https://github.com/orfjackal/jumi/blob/1eddce9866f4bcc3e9b08b9b447ab6d19f4ec1fc/jumi-core/src/test/java/fi/jumi/core/runs/ThreadBoundSuiteNotifierTest.java#L175">
     * fireTestFinished_cannot_be_called_after_the_test_is_finished</a>
     * @reference <br>[2]: <a href="https://github.com/orfjackal/jumi/blob/1eddce9866f4bcc3e9b08b9b447ab6d19f4ec1fc/jumi-core/src/test/java/fi/jumi/core/runs/ThreadBoundSuiteNotifierTest.java#L153">
     * fireTestFinished_must_be_called_on_the_current_test</a>
     */
    /* TODO: add javadoc after implemented:
       If the test started any threads, will wait for them to finish (except the AWT event thread and maybe some others).
       It is an error (or at least a warning) for a test to start threads without stopping them.
     */
    void fireTestFinished();
}
