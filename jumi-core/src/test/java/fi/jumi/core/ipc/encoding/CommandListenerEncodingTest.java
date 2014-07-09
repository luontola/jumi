// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc.encoding;

import fi.jumi.core.config.*;
import fi.jumi.core.ipc.api.CommandListener;

import java.lang.reflect.*;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CommandListenerEncodingTest extends EncodingContract<CommandListener> {

    public CommandListenerEncodingTest() {
        super(CommandListenerEncoding::new);
    }

    @Override
    protected void exampleUsage(CommandListener listener) throws Exception {
        SuiteConfiguration config = new SuiteConfigurationBuilder()
                .addToClasspath(Paths.get("foo/bar.jar"))
                .addJvmOptions("-jvmOption")
                .setWorkingDirectory(Paths.get("workingDir"))
                .setIncludedTestsPattern("glob:Included.class")
                .setExcludedTestsPattern("glob:Excluded.class")
                .freeze();
        assertNoDefaultValues(config);
        listener.runTests(config);
        listener.shutdown();
    }

    private static void assertNoDefaultValues(SuiteConfiguration config) throws Exception {
        for (Field field : SuiteConfiguration.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            Object actualValue = field.get(config);
            Object defaultValue = field.get(SuiteConfiguration.DEFAULTS);
            assertThat("'" + field.getName() + "' field was at default value", actualValue, is(not(defaultValue)));
        }
    }
}
