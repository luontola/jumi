// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import fi.jumi.launcher.JumiBootstrap;
import org.junit.*;
import org.junit.rules.ExpectedException;
import sample.*;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class JumiBootstrapTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private final StringBuilder out = new StringBuilder();

    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void runs_tests_with_current_classpath() throws Exception {
        JumiBootstrap bootstrap = new JumiBootstrap().setOut(out);

        bootstrap.runTestClasses(OnePassingTest.class);

        String out = this.out.toString();
        assertThat("should show test results", out, containsString("Pass: 2"));
        assertThat("should hide passing tests by default", out, not(containsString("OnePassingTest")));
    }

    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void reports_failures_by_throwing_AssertionError() throws Exception {
        JumiBootstrap bootstrap = new JumiBootstrap().setOut(out);

        thrown.expect(AssertionError.class);
        thrown.expectMessage("There were test failures");
        bootstrap.runTestClasses(OneFailingTest.class);
    }

    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void can_debug_the_daemons_actor_messages() throws Exception {
        ByteArrayOutputStream daemonOutput = new ByteArrayOutputStream();
        JumiBootstrap bootstrap = new JumiBootstrap().setOut(out).enableDebugMode(daemonOutput);

        bootstrap.runTestClasses(OnePassingTest.class);

        assertThat(daemonOutput.toString(), containsString("[jumi-actors-1]"));
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

        assertThat(getFieldValue(bootstrap, "out"), is((Object) System.out));
    }

    @Test
    public void debug_output_is_disabled_by_default() {
        JumiBootstrap bootstrap = new JumiBootstrap();

        assertThat(getFieldValue(bootstrap, "debugOutput"), is((Object) null));
    }

    @Test
    public void debug_output_defaults_to_stderr_when_enabled() {
        JumiBootstrap bootstrap = new JumiBootstrap().enableDebugMode();

        assertThat(getFieldValue(bootstrap, "debugOutput"), is((Object) System.err));
    }

    private static Object getFieldValue(Object object, String fieldName) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
