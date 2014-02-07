// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.util;

import org.junit.Test;

import java.nio.file.*;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ClasspathTest {

    @Test
    public void extracts_classpath_elements_on_Windows() {
        assertThat(Classpath.getClasspathElements("C:\\foo.jar;C:\\bar.jar", ";"),
                is(asList(Paths.get("C:\\foo.jar"), Paths.get("C:\\bar.jar"))));
    }

    @Test
    public void extracts_classpath_elements_on_Unix() {
        assertThat(Classpath.getClasspathElements("/foo.jar;/bar.jar", ";"),
                is(asList(Paths.get("/foo.jar"), Paths.get("/bar.jar"))));
    }

    @Test
    public void workaround_for_Eclipse_JUnit_integration_producing_invalid_classpath_elements_on_Windows() {
        // The JUnit interaction of Eclipse Kepler Service Release 1 on Windows adds to classpath
        // elements which start with "/C:" and java.nio.file.Paths.get() can't handle it.
        String troublesomePath = "/C:/eclipse/configuration/org.eclipse.osgi/bundles/200/1/.cp/";

        List<Path> classpath = Classpath.getClasspathElements(troublesomePath, ";");

        assertThat(classpath, is(asList(Paths.get("C:\\eclipse\\configuration\\org.eclipse.osgi\\bundles\\200\\1\\.cp"))));
    }
}
