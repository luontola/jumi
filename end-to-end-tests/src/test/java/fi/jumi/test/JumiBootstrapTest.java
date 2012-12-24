// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import fi.jumi.launcher.JumiBootstrap;
import org.junit.Test;
import sample.OnePassingTest;

import java.io.ByteArrayOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class JumiBootstrapTest {

    private final StringBuilder out = new StringBuilder();
    private final JumiBootstrap bootstrap = new JumiBootstrap().setOut(out);

    @Test
    public void runs_tests_with_current_classpath() throws Exception {
        bootstrap.runTestClass(OnePassingTest.class);

        assertThat(out.toString(), containsString("testPassing"));
    }

    @Test
    public void can_debug_the_daemons_actor_messages() throws Exception {
        ByteArrayOutputStream daemonOutput = new ByteArrayOutputStream();

        bootstrap.enableDebugMode(daemonOutput)
                .runTestClass(OnePassingTest.class);

        assertThat(daemonOutput.toString(), containsString("[jumi-actors-1]"));
    }
}
