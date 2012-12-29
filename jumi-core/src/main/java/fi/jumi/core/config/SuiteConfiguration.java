// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
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
    private final List<URI> classPath;
    private final List<String> jvmOptions;
    private final List<String> testClasses;
    private final String includedTestsPattern;
    private final String excludedTestsPattern;

    public SuiteConfiguration() {
        classPath = Collections.emptyList();
        jvmOptions = Collections.emptyList();
        testClasses = Collections.emptyList();
        includedTestsPattern = "glob:**Test.class";
        excludedTestsPattern = "glob:**$*.class";
    }

    SuiteConfiguration(SuiteConfigurationBuilder src) {
        classPath = Immutables.list(src.classPath());
        jvmOptions = Immutables.list(src.jvmOptions());
        testClasses = Immutables.list(src.testClasses());
        includedTestsPattern = src.includedTestsPattern();
        excludedTestsPattern = src.excludedTestsPattern();
    }

    public SuiteConfigurationBuilder melt() {
        return new SuiteConfigurationBuilder(this);
    }


    // factory methods

    public PathMatcher createTestFileMatcher(FileSystem fileSystem) {
        return new IncludeExcludePathMatcher(fileSystem, includedTestsPattern(), excludedTestsPattern());
    }


    // getters

    public List<URI> classPath() {
        return classPath;
    }

    public List<String> jvmOptions() {
        return jvmOptions;
    }

    public List<String> testClasses() {
        return testClasses;
    }

    public String includedTestsPattern() {
        return includedTestsPattern;
    }

    public String excludedTestsPattern() {
        return excludedTestsPattern;
    }
}
