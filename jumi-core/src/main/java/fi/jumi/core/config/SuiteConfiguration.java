// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.config;

import fi.jumi.core.util.*;

import javax.annotation.concurrent.Immutable;
import java.io.Serializable;
import java.net.URI;
import java.nio.file.*;
import java.util.*;

@Immutable
public class SuiteConfiguration implements Serializable {

    public static final SuiteConfiguration DEFAULTS = new SuiteConfiguration();

    // TODO: support for main and test class paths
    private final List<URI> classPath; // TODO: rename to "classpath" on next breaking change to this class
    private final List<String> jvmOptions;
    private final URI workingDirectory;
    private final String includedTestsPattern;
    private final String excludedTestsPattern;

    public SuiteConfiguration() {
        classPath = Collections.emptyList();
        jvmOptions = Collections.emptyList();
        workingDirectory = Paths.get(".").normalize().toUri();
        includedTestsPattern = "glob:**Test.class";
        excludedTestsPattern = "glob:**$*.class";
    }

    SuiteConfiguration(SuiteConfigurationBuilder src) {
        classPath = Immutables.list(src.getClassPath());
        jvmOptions = Immutables.list(src.getJvmOptions());
        workingDirectory = src.getWorkingDirectory();
        includedTestsPattern = src.getIncludedTestsPattern();
        excludedTestsPattern = src.getExcludedTestsPattern();
    }

    public SuiteConfigurationBuilder melt() {
        return new SuiteConfigurationBuilder(this);
    }


    // factory methods

    public PathMatcher createTestFileMatcher(FileSystem fileSystem) {
        return new IncludeExcludePathMatcher(fileSystem, getIncludedTestsPattern(), getExcludedTestsPattern());
    }


    // getters

    public List<URI> getClassPath() {
        return classPath;
    }

    public List<String> getJvmOptions() {
        return jvmOptions;
    }

    public URI getWorkingDirectory() {
        return workingDirectory;
    }

    public String getIncludedTestsPattern() {
        return includedTestsPattern;
    }

    public String getExcludedTestsPattern() {
        return excludedTestsPattern;
    }
}
