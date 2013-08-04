// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.api.drivers;

public interface SuiteNotifier {

    // TODO: support navigation by providing a Class or Method instance to the fireTestFound method

    /**
     * Notifies about the existence of a test.
     * <p>
     * Must be called before starting the test.<sup>[1]</sup> Must be called with parent {@code testId} before any of
     * its children.<sup>[2]</sup> Idempotent,<sup>[3]</sup> but the {@code name} must always be the same if called
     * multiple times.<sup>[4]</sup>
     *
     * @reference [1]: <a href="https://github.com/orfjackal/jumi/blob/1eddce9866f4bcc3e9b08b9b447ab6d19f4ec1fc/jumi-core/src/test/java/fi/jumi/core/runs/RunEventNormalizerTest.java#L109">
     * onTestFound_must_be_called_before_onTestStarted</a>
     * @reference <br>[2]: <a href="https://github.com/orfjackal/jumi/blob/1eddce9866f4bcc3e9b08b9b447ab6d19f4ec1fc/jumi-core/src/test/java/fi/jumi/core/runs/RunEventNormalizerTest.java#L101">
     * parents_must_be_found_before_their_children</a>
     * @reference <br>[3]: <a href="https://github.com/orfjackal/jumi/blob/1eddce9866f4bcc3e9b08b9b447ab6d19f4ec1fc/jumi-core/src/test/java/fi/jumi/core/runs/RunEventNormalizerTest.java#L82">
     * removes_duplicate_onTestFound_events</a>
     * @reference <br>[4]: <a href="https://github.com/orfjackal/jumi/blob/1eddce9866f4bcc3e9b08b9b447ab6d19f4ec1fc/jumi-core/src/test/java/fi/jumi/core/runs/RunEventNormalizerTest.java#L91">
     * tests_must_be_found_always_with_the_same_name</a>
     */
    /* TODO: add javadoc after implemented:
       IDEs can use the Class or Method object to implement code navigation.
     */
    void fireTestFound(TestId testId, String name);

    /**
     * Notifies about the beginning of a test execution.
     * <p>
     * May be called multiple times, before a test is {@linkplain TestNotifier#fireTestFinished() finished}, to produce
     * nested tests.<sup>[1]</sup> The only limitation is that a nested test must be finished before the surrounding
     * tests.<sup>[2]</sup>
     * <p>
     * Everything printed to {@link System#out}<sup>[3]</sup> and {@link System#err}<sup>[4]</sup> after the call to
     * this method<sup>[5]</sup> will be recorded from the current thread<sup>[6]</sup> and threads which are started by
     * it<sup>[7]</sup> (possibly together with timestamps and name of the thread which printed it).
     *
     * @return a notifier for the events of the test execution that was just started.
     * @reference [1]: <a href="https://github.com/orfjackal/jumi/blob/1eddce9866f4bcc3e9b08b9b447ab6d19f4ec1fc/jumi-core/src/test/java/fi/jumi/core/runs/ThreadBoundSuiteNotifierTest.java#L40">
     * notifies_about_the_beginning_and_end_of_a_run</a>
     * @reference <br>[2]: <a href="https://github.com/orfjackal/jumi/blob/1eddce9866f4bcc3e9b08b9b447ab6d19f4ec1fc/jumi-core/src/test/java/fi/jumi/core/runs/ThreadBoundSuiteNotifierTest.java#L153">
     * fireTestFinished_must_be_called_on_the_current_test</a>
     * @reference <br>[3]: <a href="https://github.com/orfjackal/jumi/blob/1eddce9866f4bcc3e9b08b9b447ab6d19f4ec1fc/jumi-core/src/test/java/fi/jumi/core/output/OutputCapturerTest.java#L57">
     * captures_stdout</a> &amp; <a href="https://github.com/orfjackal/jumi/blob/1eddce9866f4bcc3e9b08b9b447ab6d19f4ec1fc/jumi-core/src/test/java/fi/jumi/core/output/OutputCapturerInstallerTest.java#L19">
     * replaces_stdout_with_the_captured_stream</a>
     * @reference <br>[4]: <a href="https://github.com/orfjackal/jumi/blob/1eddce9866f4bcc3e9b08b9b447ab6d19f4ec1fc/jumi-core/src/test/java/fi/jumi/core/output/OutputCapturerTest.java#L68">
     * captures_stderr</a> &amp; <a href="https://github.com/orfjackal/jumi/blob/1eddce9866f4bcc3e9b08b9b447ab6d19f4ec1fc/jumi-core/src/test/java/fi/jumi/core/output/OutputCapturerInstallerTest.java#L26">
     * replaces_stderr_with_the_captured_stream</a>
     * @reference <br>[5]: <a href="https://github.com/orfjackal/jumi/blob/1eddce9866f4bcc3e9b08b9b447ab6d19f4ec1fc/jumi-core/src/test/java/fi/jumi/core/runs/ThreadBoundSuiteNotifierTest.java#L57">
     * captures_what_is_printed_during_a_run</a>
     * @reference <br>[6]: <a href="https://github.com/orfjackal/jumi/blob/1eddce9866f4bcc3e9b08b9b447ab6d19f4ec1fc/jumi-core/src/test/java/fi/jumi/core/output/OutputCapturerTest.java#L117">
     * concurrent_captures_are_isolated_from_each_other</a>
     * @reference <br>[7]: <a href="https://github.com/orfjackal/jumi/blob/1eddce9866f4bcc3e9b08b9b447ab6d19f4ec1fc/jumi-core/src/test/java/fi/jumi/core/output/OutputCapturerTest.java#L146">
     * captures_what_is_printed_in_spawned_threads</a>
     * @see TestNotifier#fireTestFinished()
     */
    TestNotifier fireTestStarted(TestId testId);

    /**
     * Notifies about an internal error in the testing framework, i.e. not a {@linkplain TestNotifier#fireFailure test
     * failure}.
     */
    void fireInternalError(String message, Throwable cause);
}
