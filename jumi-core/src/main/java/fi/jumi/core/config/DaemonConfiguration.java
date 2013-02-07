// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.config;

import javax.annotation.concurrent.Immutable;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Immutable
public class DaemonConfiguration {

    public static final DaemonConfiguration DEFAULTS = new DaemonConfiguration();

    // command line arguments
    public static final String JUMI_HOME = "--jumi-home";
    public static final String LAUNCHER_PORT = "--launcher-port";

    public static final SystemProperty IDLE_TIMEOUT = new SystemProperty("idleTimeout", "jumi.daemon.idleTimeout", DEFAULTS);
    public static final SystemProperty STARTUP_TIMEOUT = new SystemProperty("startupTimeout", "jumi.daemon.startupTimeout", DEFAULTS);
    public static final SystemProperty LOG_ACTOR_MESSAGES = new SystemProperty("logActorMessages", "jumi.daemon.logActorMessages", DEFAULTS);
    public static final List<SystemProperty> PROPERTIES = Arrays.asList(LOG_ACTOR_MESSAGES, STARTUP_TIMEOUT, IDLE_TIMEOUT);

    private final Path jumiHome;
    private final int launcherPort;
    private final boolean logActorMessages;
    private final long startupTimeout;
    private final long idleTimeout;

    public DaemonConfiguration() {
        jumiHome = Paths.get(System.getProperty("user.home"), ".jumi");
        launcherPort = 0;
        logActorMessages = false;
        startupTimeout = TimeUnit.SECONDS.toMillis(30);
        idleTimeout = TimeUnit.SECONDS.toMillis(1);  // TODO: increase to 15 min, after implementing persistent daemons
    }

    DaemonConfiguration(DaemonConfigurationBuilder src) {
        jumiHome = src.getJumiHome();
        launcherPort = src.getLauncherPort();
        logActorMessages = src.getLogActorMessages();
        startupTimeout = src.getStartupTimeout();
        idleTimeout = src.getIdleTimeout();
    }

    public DaemonConfigurationBuilder melt() {
        return new DaemonConfigurationBuilder(this);
    }


    // conversions

    public String[] toProgramArgs() {
        return new String[]{
                JUMI_HOME, getJumiHome().toString(),
                LAUNCHER_PORT, String.valueOf(getLauncherPort())
        };
    }

    public Properties toSystemProperties() {
        Properties systemProperties = new Properties();
        for (SystemProperty property : PROPERTIES) {
            property.toSystemProperty(this, systemProperties);
        }
        return systemProperties;
    }


    // getters

    public Path getJumiHome() {
        return jumiHome;
    }

    public int getLauncherPort() {
        return launcherPort;
    }

    public boolean getLogActorMessages() {
        return logActorMessages;
    }

    public long getStartupTimeout() {
        return startupTimeout;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }
}
