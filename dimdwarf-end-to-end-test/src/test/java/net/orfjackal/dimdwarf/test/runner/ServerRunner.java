// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.test.runner;

import java.util.Random;

public class ServerRunner {

    private final String host = "localhost";
    private final int port;

    public ServerRunner() {
        port = new Random().nextInt(10000) + 10000;
    }

    public void startApplication(Class<?> application) {
    }

    public void shutdown() {
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
