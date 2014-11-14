// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.io.ByteStreams;
import fi.jumi.launcher.daemon.EmbeddedDaemonJar;
import fi.jumi.test.PartiallyParameterized.NonParameterized;
import fi.luontola.buildtest.*;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.concurrent.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

import static fi.luontola.buildtest.AsmMatchers.*;
import static fi.luontola.buildtest.AsmUtils.annotatedWithOneOf;
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
    private final Deprecations expectedDeprecations;

    public BuildTest(String artifactId, List<Integer> expectedClassVersion, List<String> expectedDependencies, List<String> expectedContents, Deprecations expectedDeprecations) {
        this.artifactId = artifactId;
        this.expectedClassVersion = expectedClassVersion.toArray(new Integer[expectedClassVersion.size()]);
        this.expectedDependencies = expectedDependencies;
        this.expectedContents = expectedContents;
        this.expectedDeprecations = expectedDeprecations;
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
                                BASE_PACKAGE + "api/"),
                        new Deprecations()
                },

                {"jumi-core",
                        asList(Opcodes.V1_2, Opcodes.V1_5, Opcodes.V1_6, Opcodes.V1_7),
                        asList(
                                "fi.jumi:jumi-actors",
                                "fi.jumi:jumi-api"),
                        asList(
                                MANIFEST,
                                POM_FILES,
                                BASE_PACKAGE + "core/"),
                        new Deprecations()
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
                                BASE_PACKAGE + "daemon/"),
                        new Deprecations()
                },

                {"jumi-launcher",
                        asList(Opcodes.V1_6, Opcodes.V1_7),
                        asList(
                                "fi.jumi:jumi-core"),
                        asList(
                                MANIFEST,
                                POM_FILES,
                                BASE_PACKAGE + "launcher/"),
                        new Deprecations()
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
        JarUtils.assertContainsOnly(getProjectJar(), expectedContents);
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
        Path publishedJar = TestEnvironment.ARTIFACTS.getProjectJar("jumi-daemon").toPath();

        try (InputStream in1 = embeddedJar.getDaemonJarAsStream();
             InputStream in2 = Files.newInputStream(publishedJar)) {

            assertTrue("the embedded daemon JAR was not equal to " + publishedJar,
                    Arrays.equals(ByteStreams.toByteArray(in1), ByteStreams.toByteArray(in2)));
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

        JarUtils.checkAllClasses(getProjectJar(), matcher);
    }

    @Test
    public void all_classes_must_be_annotated_with_JSR305_concurrent_annotations() throws Exception {
        CompositeMatcher<ClassNode> matcher = newClassNodeCompositeMatcher()
                .excludeIf(nameStartsWithOneOf(DOES_NOT_NEED_JSR305_ANNOTATIONS))
                .excludeIf(is(anInterface()))
                .excludeIf(is(syntheticClass()))
                .excludeIf(is(anonymousClass()))
                .assertThatIt(is(annotatedWithOneOf(Immutable.class, NotThreadSafe.class, ThreadSafe.class)));

        JarUtils.checkAllClasses(getProjectJar(), matcher);
    }

    @Test
    public void deprecated_methods_are_removed_after_the_transition_period() throws IOException {
        // TODO: remove duplication between the "INTERNAL" here and in DOES_NOT_NEED_JSR305_ANNOTATIONS?
        expectedDeprecations.verify(
                FluentIterable.from(new ClassesInJarFile(getProjectJar()))
                        .filter(Predicates.not(cn -> cn.name.contains("/INTERNAL/"))));
    }


    // helper methods

    private File getProjectPom() throws IOException {
        return TestEnvironment.ARTIFACTS.getProjectPom(artifactId);
    }

    private File getProjectJar() throws IOException {
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

    private Properties getMavenArtifactProperties(File jarFile, String filename) {
        return JarUtils.getProperties(jarFile, POM_FILES + artifactId + "/" + filename);
    }
}
