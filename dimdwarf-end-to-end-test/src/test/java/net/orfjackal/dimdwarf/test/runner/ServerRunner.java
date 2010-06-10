// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.test.runner;

import com.google.inject.Module;
import net.orfjackal.dimdwarf.test.util.TestEnvironment;
import org.apache.commons.io.*;

import java.io.*;
import java.util.*;

public class ServerRunner {

    private final String host = "localhost";
    private final int port;
    private Process serverProcess;
    private File applicationDir;

    public ServerRunner() {
        port = new Random().nextInt(10000) + 10000;
    }

    public void startApplication(Class<? extends Module> application) throws IOException {
        deployApplication(application);
        startServer();
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
                "app.name=" + application.getSimpleName(),
                "app.module=" + application.getName()
        ));
    }

    private void startServer() throws IOException {
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(TestEnvironment.getDeploymentDir());
        builder.redirectErrorStream(false);
        builder.command("java", "-jar", "launcher.jar");

        serverProcess = builder.start();
        redirectStream(serverProcess.getInputStream(), System.out);
        redirectStream(serverProcess.getErrorStream(), System.err);
    }

    private static void redirectStream(final InputStream in, final OutputStream out) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    IOUtils.copy(in, out);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.setDaemon(true);
        t.start();
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
