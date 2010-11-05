// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package end2endtests.runner;

import com.google.inject.Module;
import end2endtests.env.TestEnvironment;
import net.orfjackal.dimdwarf.testutils.SocketUtil;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class ServerRunner {

    private static final int TIMEOUT = 5;
    private static final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;

    private static final String JAVA_EXECUTABLE = new File(System.getProperty("java.home"), "bin/java").getAbsolutePath();
    private static final List<String> JAR_TO_EXECUTE = Arrays.asList("-jar", "launcher.jar");
    private final List<String> jvmOptions = new ArrayList<String>();
    private final String host;
    private final int port;

    private File applicationDir;
    private ProcessRunner serverProcess;

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
        serverProcess = new ProcessRunner(TestEnvironment.getServerHomeDir(), commandToStartServer());
        serverProcess.start();
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

    private void waitForServerToStart() throws InterruptedException {
        // TODO: add management hooks for monitoring server state (program arguments: --management-port, --client-port)
        serverProcess.waitForOutput("Server started", TIMEOUT, TIMEOUT_UNIT);
    }

    public void assertIsRunning() {
        if (serverProcess != null) {
            assertTrue("Server had died unexpectedly", serverProcess.isAlive());
        }
    }

    public void shutdown() {
        try {
            if (serverProcess != null) {
                serverProcess.kill();
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
