// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.config;

import javax.annotation.concurrent.NotThreadSafe;
import java.nio.file.*;
import java.util.*;

@NotThreadSafe
public class DaemonConfigurationBuilder {

    // command line arguments
    private Path jumiHome;
    private int launcherPort;

    // system properties
    private int testThreadsCount;
    private boolean logActorMessages;
    private long startupTimeout;
    private long idleTimeout;

    public DaemonConfigurationBuilder() {
        this(DaemonConfiguration.DEFAULTS);
    }

    DaemonConfigurationBuilder(DaemonConfiguration src) {
        jumiHome = src.getJumiHome();
        launcherPort = src.getLauncherPort();
        if (src.isTestThreadsCountAutomatic()) {
            testThreadsCount = 0;
        } else {
            testThreadsCount = src.getTestThreadsCount();
        }
        logActorMessages = src.getLogActorMessages();
        startupTimeout = src.getStartupTimeout();
        idleTimeout = src.getIdleTimeout();
    }

    public DaemonConfiguration freeze() {
        return new DaemonConfiguration(this);
    }


    // conversions

    public DaemonConfigurationBuilder parseProgramArgs(String... args) {
        Iterator<String> it = Arrays.asList(args).iterator();
        while (it.hasNext()) {
            String parameter = it.next();
            switch (parameter) {
                case DaemonConfiguration.JUMI_HOME:
                    setJumiHome(Paths.get(it.next()));
                    break;
                case DaemonConfiguration.LAUNCHER_PORT:
                    setLauncherPort(Integer.parseInt(it.next()));
                    break;
                default:
                    throw new IllegalArgumentException("unsupported parameter: " + parameter);
            }
        }
        checkRequiredParameters();
        return this;
    }

    private void checkRequiredParameters() {
        if (getLauncherPort() <= 0) {
            throw new IllegalArgumentException("missing required parameter: " + DaemonConfiguration.LAUNCHER_PORT);
        }
    }

    public DaemonConfigurationBuilder parseSystemProperties(Properties systemProperties) {
        for (SystemProperty property : DaemonConfiguration.PROPERTIES) {
            property.parseSystemProperty(this, systemProperties);
        }
        return this;
    }


    // getters and setters

    public Path getJumiHome() {
        return jumiHome;
    }

    public DaemonConfigurationBuilder setJumiHome(Path jumiHome) {
        this.jumiHome = jumiHome;
        return this;
    }

    public int getLauncherPort() {
        return launcherPort;
    }

    public DaemonConfigurationBuilder setLauncherPort(int launcherPort) {
        this.launcherPort = launcherPort;
        return this;
    }

    public int getTestThreadsCount() {
        return testThreadsCount;
    }

    public DaemonConfigurationBuilder setTestThreadsCount(int testThreadsCount) {
        this.testThreadsCount = testThreadsCount;
        return this;
    }

    public boolean getLogActorMessages() {
        return logActorMessages;
    }

    public DaemonConfigurationBuilder setLogActorMessages(boolean logActorMessages) {
        this.logActorMessages = logActorMessages;
        return this;
    }

    public long getStartupTimeout() {
        return startupTimeout;
    }

    public DaemonConfigurationBuilder setStartupTimeout(long startupTimeout) {
        this.startupTimeout = startupTimeout;
        return this;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public DaemonConfigurationBuilder setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
        return this;
    }
}
