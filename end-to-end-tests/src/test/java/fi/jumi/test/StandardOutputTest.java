// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import org.junit.*;
import org.junit.rules.Timeout;
import sample.PrintingTest;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class StandardOutputTest {

    @Rule
    public final AppRunner app = new AppRunner();

    @Rule
    public final Timeout timeout = Timeouts.forEndToEndTest();


    @Test
    public void shows_what_tests_print_to_stdout() throws Exception {
        assertThat(outputOf(PrintingTest.class, "testPrintOut"), containsString("printed to stdout"));
    }

    @Test
    public void shows_what_tests_print_to_stderr() throws Exception {
        assertThat(outputOf(PrintingTest.class, "testPrintErr"), containsString("printed to stderr"));
    }

    @Test
    public void printing_to_stdout_and_stderr_is_synchronous() throws Exception {
        assertThat(outputOf(PrintingTest.class, "testInterleavedPrinting"), containsString("trololo"));
    }

    @Test
    public void compensates_for_the_default_charset_of_the_daemon_process() throws Exception {
        app.setDaemonDefaultCharset(StandardCharsets.ISO_8859_1);
        assertThat(outputOf(PrintingTest.class, "testPrintNonAscii"), allOf(
                containsString("default charset is ISO-8859-1"),
                containsString("åäö")));

        app.setDaemonDefaultCharset(StandardCharsets.UTF_8);
        assertThat(outputOf(PrintingTest.class, "testPrintNonAscii"), allOf(
                containsString("default charset is UTF-8"),
                containsString("åäö")));
    }

    @Test
    public void displays_all_unicode_characters_correctly() throws Exception {
        assertThat(outputOf(PrintingTest.class, "testPrintNonAscii"), containsString("\u4f60\u597d")); // 你好 nihao - hello in Chinese
    }

    // TODO: writing binary data to System.out: detect when using System.out as OutputStream and show it as binary data in the UI


    private String outputOf(Class<?> testClass, String testName) throws Exception {
        app.runTests(testClass);
        return app.getRunOutput(testClass, testName);
    }
}
