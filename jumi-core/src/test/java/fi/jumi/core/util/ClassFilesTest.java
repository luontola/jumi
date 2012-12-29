// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import org.junit.*;
import org.junit.rules.ExpectedException;

import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ClassFilesTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void converting_class_names_to_paths() {
        assertThat(ClassFiles.classNameToPath("Foo"), is("Foo.class"));
        assertThat(ClassFiles.classNameToPath("com.example.Foo"), is("com/example/Foo.class"));
    }

    @Test
    public void converting_paths_to_class_names() {
        assertThat(ClassFiles.pathToClassName(Paths.get("Foo.class")), is("Foo"));
        assertThat(ClassFiles.pathToClassName(Paths.get("com/example/Foo.class")), is("com.example.Foo"));
    }

    @Test
    public void accepts_only_class_files() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Not a class file: Foo.java");
        ClassFiles.pathToClassName(Paths.get("Foo.java"));
    }
}
