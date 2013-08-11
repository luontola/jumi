// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import com.google.common.io.*;
import fi.jumi.launcher.daemon.EmbeddedDaemonJar;
import fi.jumi.test.PartiallyParameterized.NonParameterized;
import fi.jumi.test.util.*;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.concurrent.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.*;
import java.util.*;

import static fi.jumi.test.util.AsmMatchers.*;
import static fi.jumi.test.util.AsmUtils.annotatedWithOneOf;
import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

@RunWith(PartiallyParameterized.class)
public class BuildTest {

    private static final String MANIFEST = "META-INF/MANIFEST.MF";
    private static final String POM_FILES = "META-INF/maven/fi.jumi/";
    private static final String BASE_PACKAGE = "fi/jumi/";

    private static final String[] DOES_NOT_NEED_JSR305_ANNOTATIONS = {
            // shaded classes
            "fi/jumi/core/INTERNAL/",
            "fi/jumi/daemon/INTERNAL/",
            "fi/jumi/launcher/INTERNAL/",
            // generated classes
            "fi/jumi/core/events/",
    };

    private final String artifactId;
    private final Integer[] expectedClassVersion;
    private final List<String> expectedDependencies;
    private final List<String> expectedContents;

    public BuildTest(String artifactId, List<Integer> expectedClassVersion, List<String> expectedDependencies, List<String> expectedContents) {
        this.artifactId = artifactId;
        this.expectedClassVersion = expectedClassVersion.toArray(new Integer[expectedClassVersion.size()]);
        this.expectedDependencies = expectedDependencies;
        this.expectedContents = expectedContents;
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        // TODO: upgrade shaded dependencies to Java 6/7 to benefit from their faster class loading
        return asList(new Object[][]{
                {"jumi-api",
                        asList(Opcodes.V1_7),
                        asList(),
                        asList(
                                MANIFEST,
                                POM_FILES,
                                BASE_PACKAGE + "api/")
                },

                {"jumi-core",
                        asList(Opcodes.V1_2, Opcodes.V1_5, Opcodes.V1_6, Opcodes.V1_7),
                        asList(
                                "fi.jumi:jumi-actors",
                                "fi.jumi:jumi-api"),
                        asList(
                                MANIFEST,
                                POM_FILES,
                                BASE_PACKAGE + "core/")
                },

                {"jumi-daemon",
                        asList(Opcodes.V1_2, Opcodes.V1_5, Opcodes.V1_6, Opcodes.V1_7),
                        asList(),
                        asList(
                                MANIFEST,
                                POM_FILES,
                                BASE_PACKAGE + "actors/",
                                BASE_PACKAGE + "api/",
                                BASE_PACKAGE + "core/",
                                BASE_PACKAGE + "daemon/")
                },

                {"jumi-launcher",
                        asList(Opcodes.V1_6, Opcodes.V1_7),
                        asList(
                                "fi.jumi:jumi-core"),
                        asList(
                                MANIFEST,
                                POM_FILES,
                                BASE_PACKAGE + "launcher/")
                },
        });
    }

    @Test
    public void pom_contains_only_allowed_dependencies() throws Exception {
        List<String> dependencies = MavenUtils.getRuntimeDependencies(getProjectPom());
        assertThat("dependencies of " + artifactId, dependencies, is(expectedDependencies));
    }

    @Test
    public void jar_contains_only_allowed_files() throws Exception {
        JarFileUtils.assertContainsOnly(getProjectJar(), expectedContents);
    }

    @Test
    public void jar_contains_a_pom_properties_with_the_maven_artifact_identifiers() throws IOException {
        Properties p = getPomProperties();
        assertThat("groupId", p.getProperty("groupId"), is("fi.jumi"));
        assertThat("artifactId", p.getProperty("artifactId"), is(artifactId));
        assertThat("version", p.getProperty("version"), is(TestEnvironment.VERSION_NUMBERING));
    }

    @Test
    public void release_jar_contains_build_properties_with_the_Git_revision_ID() throws IOException {
        assumeReleaseBuild();

        Properties p = getBuildProperties();
        assertThat(p.getProperty("revision")).as("revision").matches("[0-9a-f]{40}");
    }

    @Test
    @NonParameterized
    public void embedded_daemon_jar_is_exactly_the_same_as_the_published_daemon_jar() throws IOException {
        EmbeddedDaemonJar embeddedJar = new EmbeddedDaemonJar();
        Path publishedJar = TestEnvironment.ARTIFACTS.getProjectJar("jumi-daemon");

        try (InputStream in1 = embeddedJar.getDaemonJarAsStream();
             InputStream in2 = Files.newInputStream(publishedJar)) {

            assertTrue("the embedded daemon JAR was not equal to " + publishedJar,
                    ByteStreams.equal(asSupplier(in1), asSupplier(in2)));
        }
    }

    @Test
    public void none_of_the_artifacts_may_have_dependencies_to_external_libraries() {
        for (String dependency : expectedDependencies) {
            assertThat("artifact " + artifactId, dependency, startsWith("fi.jumi:"));
        }
    }

    @Test
    public void none_of_the_artifacts_may_contain_classes_from_external_libraries_without_shading_them() {
        for (String content : expectedContents) {
            assertThat("artifact " + artifactId, content, Matchers.<String>
                    either(startsWith(BASE_PACKAGE))
                    .or(startsWith(POM_FILES))
                    .or(startsWith(MANIFEST)));
        }
    }

    @Test
    public void all_classes_must_use_the_specified_bytecode_version() throws IOException {
        CompositeMatcher<ClassNode> matcher = newClassNodeCompositeMatcher()
                .assertThatIt(hasClassVersion(isOneOf(expectedClassVersion)));

        JarFileUtils.checkAllClasses(getProjectJar(), matcher);
    }

    @Test
    public void all_classes_must_be_annotated_with_JSR305_concurrent_annotations() throws Exception {
        CompositeMatcher<ClassNode> matcher = newClassNodeCompositeMatcher()
                .excludeIf(nameStartsWithOneOf(DOES_NOT_NEED_JSR305_ANNOTATIONS))
                .excludeIf(is(anInterface()))
                .excludeIf(is(syntheticClass()))
                .excludeIf(is(anonymousClass()))
                .assertThatIt(is(annotatedWithOneOf(Immutable.class, NotThreadSafe.class, ThreadSafe.class)));

        JarFileUtils.checkAllClasses(getProjectJar(), matcher);
    }


    // helper methods

    private Path getProjectPom() throws IOException {
        return TestEnvironment.ARTIFACTS.getProjectPom(artifactId);
    }

    private Path getProjectJar() throws IOException {
        return TestEnvironment.ARTIFACTS.getProjectJar(artifactId);
    }

    private void assumeReleaseBuild() throws IOException {
        String version = getPomProperties().getProperty("version");
        assumeTrue(TestEnvironment.VERSION_NUMBERING.isRelease(version));
    }

    private Properties getBuildProperties() throws IOException {
        return getMavenArtifactProperties(getProjectJar(), "build.properties");
    }

    private Properties getPomProperties() throws IOException {
        return getMavenArtifactProperties(getProjectJar(), "pom.properties");
    }

    private Properties getMavenArtifactProperties(Path jarFile, String filename) throws IOException {
        return JarFileUtils.getProperties(jarFile, POM_FILES + artifactId + "/" + filename);
    }

    private static InputSupplier<InputStream> asSupplier(final InputStream in) {
        return () -> in;
    }
}
