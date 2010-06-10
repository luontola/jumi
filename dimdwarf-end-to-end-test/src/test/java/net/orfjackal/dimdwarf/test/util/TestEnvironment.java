// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.test.util;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

public class TestEnvironment {

    private static final String FILE_NOT_SPECIFIED = "/dev/null";

    private static final AtomicInteger tempDirCounter = new AtomicInteger(0);
    private static final File sandboxDir;
    private static final File deploymentDir;
    private static final File applicationBaseJar;

    static {
        Properties p = testEnvironmentProperties();
        sandboxDir = canonicalFile(p.getProperty("test.sandbox", FILE_NOT_SPECIFIED));
        deploymentDir = canonicalFile(p.getProperty("test.deployment", FILE_NOT_SPECIFIED));
        applicationBaseJar = canonicalFile(p.getProperty("test.applicationBaseJar", FILE_NOT_SPECIFIED));
    }

    private static Properties testEnvironmentProperties() {
        InputStream in = TestEnvironment.class.getResourceAsStream("TestEnvironment.properties");
        if (in == null) {
            throw new RuntimeException("Properties not found");
        }
        try {
            Properties p = new Properties();
            p.load(in);
            return p;
        } catch (IOException e) {
            throw new RuntimeException("Reading properties failed", e);
        }
    }

    private static File canonicalFile(String path) {
        try {
            return new File(path).getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File createTempDir() {
        File dir = new File(sandboxDir, "temp-" + tempDirCounter.incrementAndGet());
        if (!dir.mkdir()) {
            throw new IllegalStateException("Directory with the same name already exists: " + dir);
        }
        return dir;
    }

    public static void deleteTempDir(File dir) {
        if (!dir.getParentFile().equals(sandboxDir)) {
            throw new IllegalArgumentException("I did not create that file, deleting it would be dangerous: " + dir);
        }
        try {
            FileUtils.forceDelete(dir);
        } catch (IOException e) {
            throw new RuntimeException("Unable to delete directory: " + dir, e);
        }
    }

    public static File getSandboxDir() {
        return sandboxDir;
    }

    public static File getDeploymentDir() {
        return deploymentDir;
    }

    public static File getApplicationBaseJar() {
        return applicationBaseJar;
    }
}
