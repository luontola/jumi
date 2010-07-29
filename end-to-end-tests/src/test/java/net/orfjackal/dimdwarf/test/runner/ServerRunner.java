// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.test.runner;

import com.google.inject.Module;
import net.orfjackal.dimdwarf.test.util.*;
import org.apache.commons.io.*;
import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.slf4j.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ServerRunner {

    private static final Logger logger = LoggerFactory.getLogger(ServerRunner.class);

    private static final int TIMEOUT = 5;
    private static final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;

    private static final String JAVA_EXECUTABLE = new File(System.getProperty("java.home"), "bin/java").getAbsolutePath();
    private static final List<String> JAR_TO_EXECUTE = Arrays.asList("-jar", "launcher.jar");
    private final List<String> jvmOptions = new ArrayList<String>();
    private final String host;
    private final int port;

    private File applicationDir;
    private Process serverProcess;
    private StreamWatcher outputWatcher;

    public ServerRunner() {
        this(SocketUtil.anyFreePort());
    }

    public ServerRunner(int port) {
        this("localhost", port);
    }

    public ServerRunner(String host, int port) {
        this.host = host;
        this.port = port;
        addJvmOptions("-ea");
    }

    public void addJvmOptions(String... options) {
        Collections.addAll(jvmOptions, options);
    }

    public void startApplication(Class<? extends Module> application) throws IOException, InterruptedException {
        deployApplication(application);
        startServer();
        waitForServerToStart();
    }

    private void deployApplication(Class<?> application) throws IOException {
        applicationDir = TestEnvironment.createTempDir();
        createAppJar();
        createAppConfig(application);
    }

    private void createAppJar() throws IOException {
        File baseJar = TestEnvironment.getApplicationBaseJar();
        File appLibs = new File(applicationDir, "lib");
        FileUtils.copyFileToDirectory(baseJar, appLibs);
    }

    private void createAppConfig(Class<?> application) throws IOException {
        File configFile = new File(applicationDir, "classes/META-INF/app.properties");
        FileUtils.writeLines(configFile, Arrays.asList(
                "dimdwarf.app.name=" + application.getSimpleName(),
                "dimdwarf.app.module=" + application.getName()
        ));
    }

    private void startServer() throws IOException {
        serverProcess = startProcess(
                TestEnvironment.getDeploymentDir(),
                commandToStartServer()
        );
        OutputStream toWatcher = streamToWatcher();
        redirectStream(serverProcess.getInputStream(), System.out, toWatcher);
        redirectStream(serverProcess.getErrorStream(), System.err, toWatcher);
    }

    private List<String> commandToStartServer() {
        List<String> arguments = Arrays.asList(
                "--port", String.valueOf(port),
                "--app", applicationDir.getPath()
        );

        List<String> command = new ArrayList<String>();
        command.add(JAVA_EXECUTABLE);
        command.addAll(jvmOptions);
        command.addAll(JAR_TO_EXECUTE);
        command.addAll(arguments);
        return command;
    }

    private static Process startProcess(File workingDir, List<String> command) throws IOException {
        logger.info("Starting process in working directory {}\n\t{}", workingDir, formatForCommandLine(command));

        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(workingDir);
        builder.redirectErrorStream(false);
        builder.command(command);
        return builder.start();
    }

    private static String formatForCommandLine(List<String> command) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < command.size(); i++) {
            if (i > 0) {
                sb.append(' ');
            }
            String arg = command.get(i);
            if (arg.contains(" ") || arg.contains("\\")) {
                arg = '"' + arg + '"';
            }
            sb.append(arg);
        }
        return sb.toString();
    }

    private OutputStream streamToWatcher() throws IOException {
        assert outputWatcher == null;
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream toWatcher = new LowLatencyPipedOutputStream(in);
        outputWatcher = new StreamWatcher(new InputStreamReader(in));
        return toWatcher;
    }

    private static void redirectStream(InputStream input, OutputStream systemOut, OutputStream toWatcher) {
        redirectStream(new TeeInputStream(input, toWatcher), new CloseShieldOutputStream(systemOut));
    }

    private static void redirectStream(final InputStream in, final OutputStream out) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    IOUtils.copy(in, out);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    IOUtils.closeQuietly(out);
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void waitForServerToStart() throws InterruptedException {
        outputWatcher.waitForLineContaining("Server started", TIMEOUT, TIMEOUT_UNIT);
    }

    public void shutdown() {
        try {
            if (serverProcess != null) {
                serverProcess.destroy();
                serverProcess.waitFor();
            }
            if (applicationDir != null) {
                TestEnvironment.deleteTempDir(applicationDir);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
