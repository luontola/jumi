// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.files;

import fi.jumi.core.config.SuiteConfiguration;

import javax.annotation.concurrent.ThreadSafe;
import java.net.URI;
import java.nio.file.*;
import java.util.*;

@ThreadSafe
public class TestClassFinderFactory {

    public static TestClassFinder create(SuiteConfiguration suite, ClassLoader classLoader) {
        List<String> testClasses = suite.testClasses();
        String testFileMatcher = suite.includedTestsPattern();

        if (!testClasses.isEmpty()) {
            // TODO: remove enumeration and only use file name patterns?
            return new EnumeratedTestClassFinder(testClasses, classLoader);

        } else if (!testFileMatcher.isEmpty()) {
            List<TestClassFinder> finders = new ArrayList<>();
            for (Path dir : getClassesDirectories(suite)) {
                PathMatcher matcher = suite.createTestFileMatcher(dir.getFileSystem());
                finders.add(new FileNamePatternTestClassFinder(matcher, dir, classLoader));
            }
            return new CompositeTestClassFinder(finders);

        } else {
            throw new IllegalArgumentException("testClasses and includedTestsPattern were both empty");
        }
    }

    public static List<Path> getClassesDirectories(SuiteConfiguration suite) {
        ArrayList<Path> dirs = new ArrayList<>();
        for (URI uri : suite.classPath()) {
            Path path = Paths.get(uri);
            if (Files.isDirectory(path)) {
                dirs.add(path);
            }
        }
        return dirs;
    }
}
