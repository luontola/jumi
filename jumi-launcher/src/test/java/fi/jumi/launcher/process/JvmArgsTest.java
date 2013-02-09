// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.process;

import org.hamcrest.*;
import org.junit.Test;

import java.nio.file.*;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class JvmArgsTest {

    @Test
    public void starts_an_executable_JAR() {
        Path executableJar = Paths.get("executable.jar");

        List<String> command = newBuilder()
                .setExecutableJar(executableJar)
                .freeze()
                .toCommand();

        assertThat(command, containsSubSequence("-jar", executableJar.toAbsolutePath().toString()));
    }

    @Test
    public void passes_program_arguments_as_is() {
        List<String> command = newBuilder()
                .setProgramArgs("foo", "bar")
                .freeze()
                .toCommand();

        assertThat(command, containsSubSequence("foo", "bar"));
    }

    @Test
    public void passes_JVM_options_as_is() {
        List<String> command = newBuilder()
                .setJvmOptions(Arrays.asList("-ea", "-mx100M"))
                .freeze()
                .toCommand();

        assertThat(command, containsSubSequence("-ea", "-mx100M"));
    }

    @Test
    public void passes_system_properties_as_JVM_options() {
        Properties p = new Properties();
        p.setProperty("foo", "bar");

        List<String> command = newBuilder()
                .setSystemProperties(p)
                .freeze()
                .toCommand();

        assertThat(command, containsSubSequence("-Dfoo=bar"));
    }

    @Test
    public void uses_specified_working_directory() {
        Path workingDir = Paths.get("working-dir");

        JvmArgs jvmArgs = newBuilder()
                .setWorkingDir(workingDir)
                .freeze();

        assertThat(jvmArgs.getWorkingDir(), is(workingDir));
    }

    @Test
    public void uses_specified_java_home() {
        Path javaHome = Paths.get("custom-jre");

        List<String> command = newBuilder()
                .setJavaHome(javaHome)
                .freeze()
                .toCommand();

        assertThat(command.get(0), is(Paths.get("custom-jre/bin/java").toAbsolutePath().toString()));
    }

    @Test
    public void defaults_to_current_java_home() {
        List<String> command = newBuilder()
                .freeze()
                .toCommand();

        assertThat(command.get(0), is(Paths.get(System.getProperty("java.home")).resolve("bin/java").toString()));
    }


    // helpers

    private JvmArgsBuilder newBuilder() {
        // dummy values for required parameters
        return new JvmArgsBuilder()
                .setExecutableJar(Paths.get("dummy.jar"));
    }

    private static Matcher<List<String>> containsSubSequence(String... expectedSubSequence) {
        final List<String> expected = Arrays.asList(expectedSubSequence);
        return new TypeSafeMatcher<List<String>>() {
            @Override
            protected boolean matchesSafely(List<String> actual) {
                for (int start = 0; start < actual.size(); start++) {
                    int end = Math.min(start + expected.size(), actual.size());
                    if (actual.subList(start, end).equals(expected)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("contains sub sequence ")
                        .appendValueList("", ", ", "", expected);
            }
        };
    }
}
