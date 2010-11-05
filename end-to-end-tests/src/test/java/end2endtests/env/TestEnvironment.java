// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package end2endtests.env;

import net.orfjackal.dimdwarf.testutils.Sandbox;

import java.io.*;
import java.util.Properties;

public class TestEnvironment {

    private static final String FILE_NOT_SPECIFIED = "/dev/null";

    private static final File sandboxDir;
    private static final File serverHomeDir;
    private static final File applicationBaseJar;
    private static final Sandbox sandbox;

    static {
        Properties p = testEnvironmentProperties();
        sandboxDir = canonicalFile(p.getProperty("test.sandbox", FILE_NOT_SPECIFIED));
        serverHomeDir = canonicalFile(p.getProperty("test.serverHome", FILE_NOT_SPECIFIED));
        applicationBaseJar = canonicalFile(p.getProperty("test.applicationBaseJar", FILE_NOT_SPECIFIED));
        sandbox = new Sandbox(sandboxDir);
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
        return sandbox.createTempDir();
    }

    public static void deleteTempDir(File dir) {
        sandbox.deleteTempDir(dir);
    }

    public static File getSandboxDir() {
        return sandboxDir;
    }

    public static File getServerHomeDir() {
        return serverHomeDir;
    }

    public static File getApplicationBaseJar() {
        return applicationBaseJar;
    }
}
