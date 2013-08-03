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

    private final List<URI> classPath; // TODO: rename to "classpath" on next breaking change to this class
    private final List<String> jvmOptions;
    private URI workingDirectory;
    private String includedTestsPattern;
    private String excludedTestsPattern;

    public SuiteConfigurationBuilder() {
        this(SuiteConfiguration.DEFAULTS);
    }

    SuiteConfigurationBuilder(SuiteConfiguration src) {
        classPath = new ArrayList<>(src.getClassPath());
        jvmOptions = new ArrayList<>(src.getJvmOptions());
        workingDirectory = src.getWorkingDirectory();
        includedTestsPattern = src.getIncludedTestsPattern();
        excludedTestsPattern = src.getExcludedTestsPattern();
    }

    public SuiteConfiguration freeze() {
        return new SuiteConfiguration(this);
    }


    // getters and setters

    public List<URI> getClassPath() {
        return classPath;
    }

    public SuiteConfigurationBuilder setClassPath(Path... files) {
        classPath.clear();
        for (Path file : files) {
            addToClassPath(file);
        }
        return this;
    }

    public SuiteConfigurationBuilder setClassPath(URI... files) {
        classPath.clear();
        for (URI file : files) {
            addToClassPath(file);
        }
        return this;
    }

    public SuiteConfigurationBuilder addToClassPath(Path file) {
        return addToClassPath(file.toUri());
    }

    public SuiteConfigurationBuilder addToClassPath(URI file) {
        classPath.add(file);
        return this;
    }

    public List<String> getJvmOptions() {
        return jvmOptions;
    }

    public SuiteConfigurationBuilder setJvmOptions(String... jvmOptions) {
        this.jvmOptions.clear();
        this.addJvmOptions(jvmOptions);
        return this;
    }

    public SuiteConfigurationBuilder addJvmOptions(String... jvmOptions) {
        this.jvmOptions.addAll(Arrays.asList(jvmOptions));
        return this;
    }

    public URI getWorkingDirectory() {
        return workingDirectory;
    }

    public SuiteConfigurationBuilder setWorkingDirectory(Path workingDirectory) {
        return setWorkingDirectory(workingDirectory.toUri());
    }

    public SuiteConfigurationBuilder setWorkingDirectory(URI workingDirectory) {
        this.workingDirectory = workingDirectory;
        return this;
    }

    public SuiteConfigurationBuilder setTestClasses(Class<?>... testClasses) {
        String[] classNames = new String[testClasses.length];
        for (int i = 0; i < testClasses.length; i++) {
            classNames[i] = testClasses[i].getName();
        }
        return setTestClasses(classNames);
    }

    public SuiteConfigurationBuilder setTestClasses(String... testClasses) {
        return setTestClasses(Arrays.asList(testClasses));
    }

    public SuiteConfigurationBuilder setTestClasses(List<String> testClasses) {
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
        setIncludedTestsPattern("glob:{" + pattern + "}");
        setExcludedTestsPattern("");
        return this;
    }

    public String getIncludedTestsPattern() {
        return includedTestsPattern;
    }

    /**
     * The parameter's format is the same in {@link java.nio.file.FileSystem#getPathMatcher(String)}
     */
    public SuiteConfigurationBuilder setIncludedTestsPattern(String syntaxAndPattern) {
        checkPathMatcherSyntaxAndPattern(syntaxAndPattern);
        this.includedTestsPattern = syntaxAndPattern;
        return this;
    }

    public String getExcludedTestsPattern() {
        return excludedTestsPattern;
    }

    /**
     * The parameter's format is the same in {@link java.nio.file.FileSystem#getPathMatcher(String)}
     */
    public SuiteConfigurationBuilder setExcludedTestsPattern(String syntaxAndPattern) {
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
