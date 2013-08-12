// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher;

import fi.jumi.core.config.*;
import fi.jumi.launcher.ui.*;
import org.apache.commons.io.output.*;

import javax.annotation.WillClose;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

@NotThreadSafe
public class JumiBootstrap {

    public static void main(String[] args) throws Exception {
        try {
            JumiBootstrap bootstrap = new JumiBootstrap();
            bootstrap.suite
                    .addJvmOptions("-ea")
                    .setTestClasses(args);
            bootstrap.runSuite();
        } catch (AssertionError e) {
            System.exit(1);
        }
    }

    public SuiteConfigurationBuilder suite = new SuiteConfigurationBuilder().setClasspath(currentClasspath());
    public DaemonConfigurationBuilder daemon = new DaemonConfigurationBuilder();

    private boolean passingTestsVisible = false;
    private Appendable textUiOutput = System.out;
    private OutputStream daemonOutput = new NullOutputStream();

    public JumiBootstrap setPassingTestsVisible(boolean passingTestsVisible) {
        this.passingTestsVisible = passingTestsVisible;
        return this;
    }

    public JumiBootstrap setTextUiOutput(Appendable textUiOutput) {
        this.textUiOutput = textUiOutput;
        return this;
    }

    public JumiBootstrap setDaemonOutput(@WillClose OutputStream daemonOutput) {
        this.daemonOutput = daemonOutput;
        return this;
    }

    public JumiBootstrap enableDebugMode() {
        return enableDebugMode(new CloseShieldOutputStream(System.err));
    }

    public JumiBootstrap enableDebugMode(@WillClose OutputStream daemonOutput) {
        setDaemonOutput(daemonOutput);
        this.daemon.setLogActorMessages(true);
        return this;
    }

    public static Path[] currentClasspath() {
        Path javaHome = Paths.get(System.getProperty("java.home"));

        List<Path> classpath = new ArrayList<>();
        String pathSeparator = System.getProperty("path.separator");
        for (String library : System.getProperty("java.class.path").split(Pattern.quote(pathSeparator))) {
            Path libraryPath = Paths.get(library);
            if (!libraryPath.startsWith(javaHome)) {
                classpath.add(libraryPath);
            }
        }
        return classpath.toArray(new Path[classpath.size()]);
    }

    public void runSuite() throws IOException, InterruptedException {
        runSuite(suite.freeze(), daemon.freeze());
    }

    public void runSuite(SuiteConfiguration suite, DaemonConfiguration daemon) throws IOException, InterruptedException {
        try (JumiLauncher launcher = createLauncher()) {
            launcher.start(suite, daemon);

            TextUI ui = new TextUI(launcher.getEventStream(), new PlainTextPrinter(textUiOutput));
            ui.setPassingTestsVisible(passingTestsVisible);
            ui.updateUntilFinished();

            if (ui.hasFailures()) {
                throw new AssertionError("There were test failures");
            }
        }
    }

    private JumiLauncher createLauncher() {
        return new JumiLauncherBuilder() {
            @Override
            protected OutputStream createDaemonOutputListener() {
                return daemonOutput;
            }
        }.build();
    }
}
