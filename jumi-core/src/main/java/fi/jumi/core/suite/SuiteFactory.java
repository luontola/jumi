// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.suite;

import fi.jumi.actors.*;
import fi.jumi.core.api.SuiteListener;
import fi.jumi.core.config.SuiteConfiguration;
import fi.jumi.core.discovery.*;
import fi.jumi.core.drivers.*;
import fi.jumi.core.output.OutputCapturer;
import fi.jumi.core.runs.RunIdSequence;
import fi.jumi.core.util.Startable;

import javax.annotation.concurrent.NotThreadSafe;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executor;

@NotThreadSafe
public class SuiteFactory {

    private final ActorThread actorThread;
    private final OutputCapturer outputCapturer;
    private final Executor testExecutor;

    public SuiteFactory(ActorThread actorThread, OutputCapturer outputCapturer, Executor testExecutor) {
        this.actorThread = actorThread;
        this.outputCapturer = outputCapturer;
        this.testExecutor = testExecutor;
    }

    public ActorRef<Startable> createSuiteRunner(SuiteListener suiteListener, SuiteConfiguration suite) {
        ClassLoader classLoader = createClassLoader(suite.classPath());
        TestFileFinder testFileFinder = createTestFileFinder(suite);
        DriverFinder driverFinder = new RunViaAnnotationDriverFinder();
        RunIdSequence runIdSequence = new RunIdSequence();

        return actorThread.bindActor(Startable.class,
                new SuiteRunner(
                        new DriverFactory(suiteListener, actorThread, outputCapturer, driverFinder, runIdSequence, classLoader),
                        suiteListener,
                        testFileFinder,
                        actorThread,
                        testExecutor
                ));
    }

    private static ClassLoader createClassLoader(List<URI> classpath) {
        try {
            return new URLClassLoader(asUrls(classpath));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Failed to create class loader for classpath " + classpath, e);
        }
    }

    private static URL[] asUrls(List<URI> uris) throws MalformedURLException {
        URL[] urls = new URL[uris.size()];
        for (int i = 0, filesLength = uris.size(); i < filesLength; i++) {
            urls[i] = uris.get(i).toURL();
        }
        return urls;
    }

    private static TestFileFinder createTestFileFinder(SuiteConfiguration suite) {
        List<Path> classDirectories = getClassDirectories(suite);
        List<TestFileFinder> finders = new ArrayList<>();
        for (Path dir : classDirectories) {
            PathMatcher matcher = suite.createTestFileMatcher(dir.getFileSystem());
            finders.add(new PathMatcherTestFileFinder(matcher, dir));
        }
        return new CompositeTestFileFinder(finders);
    }

    public static List<Path> getClassDirectories(SuiteConfiguration suite) {
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
