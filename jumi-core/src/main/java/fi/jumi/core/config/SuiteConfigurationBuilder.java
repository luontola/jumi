// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.config;

import fi.jumi.core.util.ClassFiles;

import javax.annotation.concurrent.NotThreadSafe;
import java.net.URI;
import java.nio.file.*;
import java.util.*;

@NotThreadSafe
public class SuiteConfigurationBuilder {

    private final List<URI> classPath;
    private final List<String> jvmOptions;
    private URI workingDirectory;
    private String includedTestsPattern;
    private String excludedTestsPattern;

    public SuiteConfigurationBuilder() {
        this(SuiteConfiguration.DEFAULTS);
    }

    SuiteConfigurationBuilder(SuiteConfiguration src) {
        classPath = new ArrayList<>(src.classPath());
        jvmOptions = new ArrayList<>(src.jvmOptions());
        workingDirectory = src.workingDirectory();
        includedTestsPattern = src.includedTestsPattern();
        excludedTestsPattern = src.excludedTestsPattern();
    }

    public SuiteConfiguration freeze() {
        return new SuiteConfiguration(this);
    }


    // getters and setters

    public List<URI> classPath() {
        return classPath;
    }

    public SuiteConfigurationBuilder addToClassPath(Path file) {
        return addToClassPath(file.toUri());
    }

    public SuiteConfigurationBuilder addToClassPath(URI file) {
        classPath.add(file);
        return this;
    }

    public List<String> jvmOptions() {
        return jvmOptions;
    }

    public SuiteConfigurationBuilder addJvmOptions(String... jvmOptions) {
        this.jvmOptions.addAll(Arrays.asList(jvmOptions));
        return this;
    }

    public URI workingDirectory() {
        return workingDirectory;
    }

    public SuiteConfigurationBuilder workingDirectory(Path workingDirectory) {
        return workingDirectory(workingDirectory.toUri());
    }

    public SuiteConfigurationBuilder workingDirectory(URI workingDirectory) {
        this.workingDirectory = workingDirectory;
        return this;
    }

    public SuiteConfigurationBuilder testClasses(String... testClasses) {
        return testClasses(Arrays.asList(testClasses));
    }

    public SuiteConfigurationBuilder testClasses(List<String> testClasses) {
        List<String> paths = new ArrayList<>();
        for (String testClass : testClasses) {
            paths.add(ClassFiles.classNameToPath(testClass));
        }
        StringBuilder pattern = new StringBuilder();
        for (String path : paths) {
            if (pattern.length() > 0) {
                pattern.append(',');
            }
            pattern.append(path);
        }
        includedTestsPattern("glob:{" + pattern + "}");
        excludedTestsPattern("");
        return this;
    }

    public String includedTestsPattern() {
        return includedTestsPattern;
    }

    /**
     * The parameter's format is the same in {@link java.nio.file.FileSystem#getPathMatcher(String)}
     */
    public SuiteConfigurationBuilder includedTestsPattern(String syntaxAndPattern) {
        checkPathMatcherSyntaxAndPattern(syntaxAndPattern);
        this.includedTestsPattern = syntaxAndPattern;
        return this;
    }

    public String excludedTestsPattern() {
        return excludedTestsPattern;
    }

    /**
     * The parameter's format is the same in {@link java.nio.file.FileSystem#getPathMatcher(String)}
     */
    public SuiteConfigurationBuilder excludedTestsPattern(String syntaxAndPattern) {
        if (!syntaxAndPattern.isEmpty()) {
            checkPathMatcherSyntaxAndPattern(syntaxAndPattern);
        }
        this.excludedTestsPattern = syntaxAndPattern;
        return this;
    }

    private static void checkPathMatcherSyntaxAndPattern(String syntaxAndPattern) {
        FileSystems.getDefault().getPathMatcher(syntaxAndPattern);
    }
}
