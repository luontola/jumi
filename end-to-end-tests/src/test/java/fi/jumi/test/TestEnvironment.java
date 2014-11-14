// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import fi.luontola.buildtest.*;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class TestEnvironment {

    public static final VersionNumbering VERSION_NUMBERING = new VersionNumbering();
    public static final ProjectArtifacts ARTIFACTS;
    private static final Path SANDBOX_DIR;
    private static final Path SAMPLE_CLASSES_DIR;
    private static final Path EXTRA_CLASSPATH;

    static {
        try (InputStream in = BuildTest.class.getResourceAsStream("/testing.properties")) {
            Properties testing = new Properties();
            testing.load(in);

            ARTIFACTS = new ProjectArtifacts(getDirectory(testing, "test.projectArtifactsDir").toFile(),
                    new VersionNumbering("\\d+.*", "")); // relaxed pattern to match both our and JUnit's version numbering
            SANDBOX_DIR = getDirectory(testing, "test.sandbox");
            SAMPLE_CLASSES_DIR = getDirectory(testing, "test.sampleClasses");
            EXTRA_CLASSPATH = getDirectory(testing, "test.extraClasspath");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Path getDirectory(Properties properties, String key) throws IOException {
        Path path = Paths.get(filteredProperty(properties, key)).toAbsolutePath();
        Files.createDirectories(path);
        return path;
    }

    private static String filteredProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value.startsWith("${")) {
            throw new IllegalStateException("the property '" + key + "' was not filled in: " + value);
        }
        return value;
    }

    public static Path getSandboxDir() {
        return SANDBOX_DIR;
    }

    public static Path getSampleClassesDir() {
        return SAMPLE_CLASSES_DIR;
    }

    public static Path getExtraClasspath() {
        return EXTRA_CLASSPATH;
    }

    public static Path getThreadSafetyAgentJar() throws IOException {
        return ARTIFACTS.getProjectJar("thread-safety-agent").toPath();
    }

    public static Path getSimpleUnitJar() throws IOException {
        return ARTIFACTS.getProjectJar("simpleunit").toPath();
    }

    public static Path getJUnitJar() throws IOException {
        return ARTIFACTS.getProjectJar("junit").toPath();
    }
}
