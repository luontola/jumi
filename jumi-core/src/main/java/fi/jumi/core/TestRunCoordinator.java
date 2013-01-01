// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core;

import fi.jumi.actors.*;
import fi.jumi.core.api.SuiteListener;
import fi.jumi.core.config.SuiteConfiguration;
import fi.jumi.core.discovery.*;
import fi.jumi.core.drivers.*;
import fi.jumi.core.output.OutputCapturer;
import fi.jumi.core.suite.SuiteRunner;
import fi.jumi.core.util.Startable;

import javax.annotation.concurrent.NotThreadSafe;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executor;

@NotThreadSafe
public class TestRunCoordinator implements CommandListener {

    // TODO: this class is untested and its role is unclear

    private final ActorThread actorThread;
    private final Executor testExecutor;
    private final Runnable shutdownHook;
    private final OutputCapturer outputCapturer;

    private SuiteListener listener = null;

    // XXX: too many constructor parameters, could we group some of them together?
    public TestRunCoordinator(ActorThread actorThread, Executor testExecutor, Runnable shutdownHook, OutputCapturer outputCapturer) {
        this.testExecutor = testExecutor;
        this.actorThread = actorThread;
        this.shutdownHook = shutdownHook;
        this.outputCapturer = outputCapturer;
    }

    @Override
    public void addSuiteListener(SuiteListener listener) {
        // XXX: Setters like this are messy. Could we get rid of this after moving to memory-mapped files?
        this.listener = listener;
    }

    @Override
    public void runTests(SuiteConfiguration suite) {
        ClassLoader classLoader = createClassLoader(suite.classPath());
        TestFileFinder testFileFinder = createTestFileFinder(suite);
        DriverFinder driverFinder = new RunViaAnnotationDriverFinder();

        ActorRef<Startable> suiteRunner = actorThread.bindActor(Startable.class,
                new SuiteRunner(listener, classLoader, testFileFinder, driverFinder, actorThread, testExecutor, outputCapturer));
        suiteRunner.tell().start();
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

    private static ClassLoader createClassLoader(List<URI> classpath) {
        try {
            return new URLClassLoader(asUrls(classpath));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Failed to create class loader for classpath " + classpath, e);
        }
    }

    static List<Path> getClassDirectories(SuiteConfiguration suite) {
        ArrayList<Path> dirs = new ArrayList<>();
        for (URI uri : suite.classPath()) {
            Path path = Paths.get(uri);
            if (Files.isDirectory(path)) {
                dirs.add(path);
            }
        }
        return dirs;
    }

    private static URL[] asUrls(List<URI> uris) throws MalformedURLException {
        URL[] urls = new URL[uris.size()];
        for (int i = 0, filesLength = uris.size(); i < filesLength; i++) {
            urls[i] = uris.get(i).toURL();
        }
        return urls;
    }

    @Override
    public void shutdown() {
        shutdownHook.run();
    }
}
