// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import fi.jumi.launcher.JumiBootstrap;
import org.junit.*;
import org.junit.rules.*;
import sample.*;

import java.io.ByteArrayOutputStream;

import static fi.jumi.core.util.ReflectionUtil.getFieldValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class JumiBootstrapTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none().handleAssertionErrors();

    @Rule
    public final Timeout timeout = new Timeout(Timeouts.END_TO_END_TEST);


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
    public void debug_output_defaults_to_stderr_when_enabled() {
        JumiBootstrap bootstrap = new JumiBootstrap().enableDebugMode();

        assertThat(getFieldValue(bootstrap, "daemonOutput"), is((Object) System.err));
        assertThat(bootstrap.daemon.getLogActorMessages(), is(true));
    }
}
