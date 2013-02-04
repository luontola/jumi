// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.daemon;

import fi.jumi.core.config.*;
import fi.jumi.core.network.*;
import fi.jumi.core.output.*;
import fi.jumi.core.suite.SuiteFactory;
import fi.jumi.core.util.timeout.*;

import javax.annotation.concurrent.ThreadSafe;
import java.io.*;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

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
                SHUTDOWN_ON_STARTUP_TIMEOUT, config.startupTimeout(), TimeUnit.MILLISECONDS
        );
        startupTimeout.start();
        Timeout idleTimeout = new CommandExecutingTimeout(
                SHUTDOWN_ON_IDLE_TIMEOUT, config.idleTimeout(), TimeUnit.MILLISECONDS
        );

        // replacing System.out/err with the output capturer
        OutputCapturer outputCapturer = new OutputCapturer(stdout, stderr, Charset.defaultCharset());
        new OutputCapturerInstaller(new SystemOutErr()).install(outputCapturer);

        // entry point of the application
        SuiteFactory suiteFactory = new SuiteFactory(config, outputCapturer, stdout);

        NetworkClient client = new NettyNetworkClient();
        client.connect("127.0.0.1", config.launcherPort(),
                new DaemonNetworkEndpoint(suiteFactory, SHUTDOWN_ON_USER_COMMAND, startupTimeout, idleTimeout));
    }
}
