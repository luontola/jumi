// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.daemon;

import fi.jumi.actors.*;
import fi.jumi.actors.eventizers.dynamic.DynamicEventizerProvider;
import fi.jumi.actors.listeners.*;
import fi.jumi.core.api.SuiteListener;
import fi.jumi.core.config.*;
import fi.jumi.core.ipc.CommandsDirectoryObserver;
import fi.jumi.core.ipc.api.CommandListener;
import fi.jumi.core.ipc.dirs.DaemonDir;
import fi.jumi.core.network.*;
import fi.jumi.core.stdout.*;
import fi.jumi.core.suite.SuiteFactory;
import fi.jumi.core.util.PrefixedThreadFactory;
import fi.jumi.core.util.timeout.*;

import javax.annotation.concurrent.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.concurrent.*;

@ThreadSafe
public class Main {

    private static final SystemExit SHUTDOWN_ON_STARTUP_TIMEOUT = new SystemExit("timed out before anybody connected");
    private static final SystemExit SHUTDOWN_ON_IDLE_TIMEOUT = new SystemExit("timed out after everybody disconnected");
    private static final SystemExit SHUTDOWN_ON_USER_COMMAND = new SystemExit("ordered to shut down");

    // Guaranteed to be the original stdout and stderr instances, even after installing the output capturer
    private static final PrintStream stdout = System.out;
    private static final PrintStream stderr = System.err;

    public static void main(String[] args) throws IOException {
        stdout.println("Jumi " + DaemonArtifact.getVersion() + " starting up");

        DaemonConfiguration config = new DaemonConfigurationBuilder()
                .parseProgramArgs(args)
                .parseSystemProperties(System.getProperties())
                .freeze();

        // timeouts for shutting down this daemon process
        Timeout startupTimeout = new CommandExecutingTimeout(
                SHUTDOWN_ON_STARTUP_TIMEOUT, config.getStartupTimeout(), TimeUnit.MILLISECONDS
        );
        startupTimeout.start();
        Timeout idleTimeout = new CommandExecutingTimeout(
                SHUTDOWN_ON_IDLE_TIMEOUT, config.getIdleTimeout(), TimeUnit.MILLISECONDS
        );

        // replacing System.out/err with the output capturer
        OutputCapturer outputCapturer = new OutputCapturer(stdout, stderr, Charset.defaultCharset());
        new OutputCapturerInstaller(new SystemOutErr()).install(outputCapturer);

        // logging
        PrintStream logOutput = stdout;
        MessageListener actorMessageLogger = config.getLogActorMessages()
                ? new PrintStreamMessageLogger(logOutput)
                : new NullMessageListener();

        // entry point of the application
        SuiteFactory suiteFactory = new SuiteFactory(config, outputCapturer, logOutput, actorMessageLogger);

        // listen for commands through IPC files
        DaemonDir daemonDir = new DaemonDir(config.getDaemonDir());
        Executor executor = Executors.newCachedThreadPool(new PrefixedThreadFactory("jumi-ipc-"));
        MultiThreadedActors actors = new MultiThreadedActors(
                executor,
                new DynamicEventizerProvider(), // TODO: use ComposedEventizerProvider
                new PrintStreamFailureLogger(logOutput),
                actorMessageLogger
        );
        executor.execute(new CommandsDirectoryObserver(daemonDir, executor, actors.startActorThread(), new MyCommandListener(suiteFactory)));

        // listen for commands through network sockets
        NetworkClient client = new NettyNetworkClient();
        client.connect("127.0.0.1", config.getLauncherPort(),
                new DaemonNetworkEndpoint(suiteFactory, SHUTDOWN_ON_USER_COMMAND, startupTimeout, idleTimeout, daemonDir));
    }

    @NotThreadSafe
    private static class MyCommandListener implements CommandListener {
        private final SuiteFactory suiteFactory;

        public MyCommandListener(SuiteFactory suiteFactory) {
            this.suiteFactory = suiteFactory;
        }
        // XXX: this should be used as an actor (it works now just because we only send one message to the daemon)

        @Override
        public void runTests(SuiteConfiguration suiteConfiguration, ActorRef<SuiteListener> suiteListener) {
            suiteFactory.configure(suiteConfiguration);
            suiteFactory.start(suiteListener.tell());
        }

        @Override
        public void shutdown() {
            SHUTDOWN_ON_USER_COMMAND.run();
        }
    }
}
