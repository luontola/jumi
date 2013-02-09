// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.process;

import javax.annotation.concurrent.NotThreadSafe;
import java.nio.file.*;
import java.util.*;

@NotThreadSafe
public class JvmArgsBuilder {

    private Path workingDir;
    private Path javaHome = Paths.get(System.getProperty("java.home"));
    private List<String> jvmOptions = new ArrayList<>();
    private Properties systemProperties = new Properties();
    private Path executableJar;
    private String[] programArgs = new String[0];

    public JvmArgs freeze() {
        return new JvmArgs(this);
    }


    // getters and setters

    public Path getExecutableJar() {
        return executableJar;
    }

    public JvmArgsBuilder setExecutableJar(Path executableJar) {
        this.executableJar = executableJar;
        return this;
    }

    public Path getJavaHome() {
        return javaHome;
    }

    public JvmArgsBuilder setJavaHome(Path javaHome) {
        this.javaHome = javaHome;
        return this;
    }

    public List<String> getJvmOptions() {
        return jvmOptions;
    }

    public JvmArgsBuilder setJvmOptions(List<String> jvmOptions) {
        this.jvmOptions = jvmOptions;
        return this;
    }

    public String[] getProgramArgs() {
        return programArgs;
    }

    public JvmArgsBuilder setProgramArgs(String... programArgs) {
        this.programArgs = programArgs;
        return this;
    }

    public Properties getSystemProperties() {
        return systemProperties;
    }

    public JvmArgsBuilder setSystemProperties(Properties systemProperties) {
        this.systemProperties = systemProperties;
        return this;
    }

    public Path getWorkingDir() {
        return workingDir;
    }

    public JvmArgsBuilder setWorkingDir(Path workingDir) {
        this.workingDir = workingDir;
        return this;
    }
}
