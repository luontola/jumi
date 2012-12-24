// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import fi.jumi.launcher.JumiBootstrap;
import org.junit.Test;
import sample.OnePassingTest;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class JumiBootstrapTest {

    private final StringBuilder out = new StringBuilder();

    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void runs_tests_with_current_classpath() throws Exception {
        JumiBootstrap bootstrap = new JumiBootstrap().setOut(out);

        bootstrap.runTestClasses(OnePassingTest.class);

        assertThat(out.toString(), containsString("testPassing"));
    }

    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void can_debug_the_daemons_actor_messages() throws Exception {
        ByteArrayOutputStream daemonOutput = new ByteArrayOutputStream();
        JumiBootstrap bootstrap = new JumiBootstrap().setOut(out).enableDebugMode(daemonOutput);

        bootstrap.runTestClasses(OnePassingTest.class);

        assertThat(daemonOutput.toString(), containsString("[jumi-actors-1]"));
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
