// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.test.runner;

import com.google.inject.Module;
import net.orfjackal.dimdwarf.test.util.*;
import org.apache.commons.io.*;
import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.io.output.CloseShieldOutputStream;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ServerRunner {

    private static final int TIMEOUT = 5;
    private static final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;

    private final String host = "localhost";
    private final int port;
    private File applicationDir;
    private Process serverProcess;
    private StreamWatcher outputWatcher;

    public ServerRunner() {
        // TODO: a smarter way to find an available port?
        port = new Random().nextInt(10000) + 10000;
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
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(TestEnvironment.getDeploymentDir());
        builder.redirectErrorStream(false);
        builder.command(
                "java", "-jar", "launcher.jar",
                "--port", String.valueOf(port),
                "--app", applicationDir.getPath()
        );
        serverProcess = builder.start();

        PipedOutputStream toWatcher = streamToWatcher();
        redirectStream(serverProcess.getInputStream(), System.out, toWatcher);
        redirectStream(serverProcess.getErrorStream(), System.err, toWatcher);
    }

    private PipedOutputStream streamToWatcher() throws IOException {
        assert outputWatcher == null;
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream toWatcher = new PipedOutputStream(in);
        outputWatcher = new StreamWatcher(new InputStreamReader(in));
        return toWatcher;
    }

    private static void redirectStream(InputStream input, OutputStream systemOut, PipedOutputStream toWatcher) {
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
