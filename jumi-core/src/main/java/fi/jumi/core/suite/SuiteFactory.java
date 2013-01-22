// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.suite;

import fi.jumi.actors.*;
import fi.jumi.actors.eventizers.ComposedEventizerProvider;
import fi.jumi.actors.listeners.*;
import fi.jumi.core.api.SuiteListener;
import fi.jumi.core.config.*;
import fi.jumi.core.discovery.*;
import fi.jumi.core.drivers.*;
import fi.jumi.core.events.*;
import fi.jumi.core.output.OutputCapturer;
import fi.jumi.core.runs.RunIdSequence;
import fi.jumi.core.util.*;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.PrintStream;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

@NotThreadSafe
public class SuiteFactory {

    private final DaemonConfiguration config;
    private final OutputCapturer outputCapturer;
    private final PrintStream logOutput;

    // some fields are package-private for testing purposes

    private ExecutorService actorsThreadPool;
    ExecutorService testsThreadPool;
    ClassLoader testClassLoader;
    private TestFileFinder testFileFinder;
    private CompositeDriverFinder driverFinder;
    private RunIdSequence runIdSequence;

    public SuiteFactory(DaemonConfiguration daemonConfiguration, OutputCapturer outputCapturer, PrintStream logOutput) {
        this.config = daemonConfiguration;
        this.outputCapturer = outputCapturer;
        this.logOutput = logOutput;
    }

    public void configure(SuiteConfiguration suite) {
        testClassLoader = createClassLoader(suite.classPath());
        testFileFinder = createTestFileFinder(suite);
        driverFinder = DriverFinderFactory.createDriverFinder(testClassLoader);
        runIdSequence = new RunIdSequence();

        // thread pool configuration
        actorsThreadPool = Executors.newCachedThreadPool(new PrefixedThreadFactory("jumi-actors-"));
        // TODO: make the number of test threads by default the number of CPUs + 1 or similar
        testsThreadPool = Executors.newFixedThreadPool(4, new PrefixedThreadFactory("jumi-tests-"));
    }

    public void start(SuiteListener listener) {

        // logging configuration
        FailureHandler failureHandler = new PrintStreamFailureLogger(logOutput);
        MessageListener messageListener = config.logActorMessages()
                ? new PrintStreamMessageLogger(logOutput)
                : new NullMessageListener();

        // actor messages are already logged by the actors container, but the test thread pool must be hooked separately
        Executor testsThreadPool = messageListener.getListenedExecutor(this.testsThreadPool);

        // actors configuration
        // TODO: not all of these eventizers might be needed - create a statistics gathering EventizerProvider
        MultiThreadedActors actors = new MultiThreadedActors(
                actorsThreadPool,
                new ComposedEventizerProvider(
                        new StartableEventizer(),
                        new RunnableEventizer(),
                        new WorkerListenerEventizer(),
                        new TestFileFinderListenerEventizer(),
                        new SuiteListenerEventizer(),
                        new CommandListenerEventizer(),
                        new RunListenerEventizer()
                ),
                failureHandler,
                messageListener
        );

        // bootstrap the system
        ActorThread actorThread = actors.startActorThread();
        ActorRef<Startable> suiteRunner = actorThread.bindActor(Startable.class,
                new SuiteRunner(
                        new DriverFactory(listener, actorThread, outputCapturer, driverFinder, runIdSequence, testClassLoader),
                        listener,
                        testFileFinder,
                        actorThread,
                        testsThreadPool
                ));
        suiteRunner.tell().start();
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
