// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import fi.jumi.launcher.JumiBootstrap;
import org.apache.commons.io.output.NullWriter;
import org.junit.*;
import org.junit.rules.*;
import sample.*;

import java.io.*;
import java.lang.reflect.Field;

import static fi.jumi.core.util.ReflectionUtil.getFieldValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public class JumiBootstrapTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none().handleAssertionErrors();

    @Rule
    public final Timeout timeout = Timeouts.forEndToEndTest();


    private final StringBuilder out = new StringBuilder();

    @Test
    public void runs_tests_with_current_classpath() throws Exception {
        JumiBootstrap bootstrap = new JumiBootstrap().setTextUiOutput(out);
        bootstrap.suite.setTestClasses(OnePassingTest.class);

        bootstrap.runSuite();

        String out = this.out.toString();
        assertThat("should show test results", out, containsString("Pass: 2"));
        assertThat("should hide passing tests by default", out, not(containsString("OnePassingTest")));
    }

    @Test
    public void reports_failures_by_throwing_AssertionError() throws Exception {
        JumiBootstrap bootstrap = new JumiBootstrap().setTextUiOutput(out);
        bootstrap.suite.setTestClasses(OneFailingTest.class);

        thrown.expect(AssertionError.class);
        thrown.expectMessage("There were test failures");
        bootstrap.runSuite();
    }

    @Test
    public void can_debug_the_daemons_actor_messages() throws Exception {
        ByteArrayOutputStream daemonOutput = new ByteArrayOutputStream();
        JumiBootstrap bootstrap = new JumiBootstrap().setTextUiOutput(out).enableDebugMode(daemonOutput);
        bootstrap.suite.setTestClasses(OnePassingTest.class);

        bootstrap.runSuite();

        assertThat(daemonOutput.toString(), containsString("[jumi-actor-1]"));
    }

    @Test
    public void will_not_close_stderr_when_debug_output_is_enabled() throws Exception {
        PrintStream originalErr = System.err;
        try {
            ByteArrayOutputStream printed = new ByteArrayOutputStream();
            PrintStream spiedErr = spy(new PrintStream(printed));
            System.setErr(spiedErr);

            JumiBootstrap bootstrap = new JumiBootstrap();
            bootstrap.suite.setTestClasses(OnePassingTest.class);
            bootstrap.daemon.setIdleTimeout(0); // we want the daemon process to exit quickly
            bootstrap.setTextUiOutput(new NullWriter());
            bootstrap.enableDebugMode(); // <-- the thing we are testing
            bootstrap.runSuite();

            Thread.sleep(50); // wait for the daemon process to exit, and our printer thread to notice it
            assertThat("this test has a problem; daemon printed nothing", printed.size(), is(not(0)));
            verify(spiedErr, never()).close(); // <-- the thing we are testing

        } finally {
            System.setErr(originalErr);
        }
    }


    // configuration

    @Test
    public void passing_tests_are_hidden_by_default() {
        JumiBootstrap bootstrap = new JumiBootstrap();

        assertThat(getFieldValue(bootstrap, "passingTestsVisible"), is((Object) false));
    }

    @Test
    public void passing_tests_can_be_made_visible() {
        JumiBootstrap bootstrap = new JumiBootstrap().setPassingTestsVisible(true);

        assertThat(getFieldValue(bootstrap, "passingTestsVisible"), is((Object) true));
    }

    @Test
    public void output_defaults_to_stdout() {
        JumiBootstrap bootstrap = new JumiBootstrap();

        assertThat(getFieldValue(bootstrap, "textUiOutput"), is((Object) System.out));
    }

    @Test
    public void debug_output_is_disabled_by_default() {
        JumiBootstrap bootstrap = new JumiBootstrap();

        assertThat(getFieldValue(bootstrap, "daemonOutput").getClass().getSimpleName(), is("NullOutputStream"));
        assertThat(bootstrap.daemon.getLogActorMessages(), is(false));
    }

    @Test
    public void debug_output_defaults_to_stderr_when_enabled() throws Exception {
        JumiBootstrap bootstrap = new JumiBootstrap().enableDebugMode();

        assertThat(getDaemonOutput(bootstrap), is((Object) System.err));
        assertThat(bootstrap.daemon.getLogActorMessages(), is(true));
    }

    private static Object getDaemonOutput(JumiBootstrap bootstrap) throws Exception {
        Object out = getFieldValue(bootstrap, "daemonOutput");

        // JumiBootstrap wraps System.err into a CloseShieldOutputStream, so this code will unwrap it
        FilterOutputStream wrapper = (FilterOutputStream) out;
        Field f = FilterOutputStream.class.getDeclaredField("out");
        f.setAccessible(true);
        return f.get(wrapper);
    }
}
