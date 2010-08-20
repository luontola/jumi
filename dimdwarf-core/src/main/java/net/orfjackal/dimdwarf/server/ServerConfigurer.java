// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.server;

import com.google.inject.Inject;

public class ServerConfigurer {

    private final ServerStarter server;

    @Inject
    public ServerConfigurer(ServerStarter server) {
        this.server = server;
    }

    public void configure(String[] args) {
        // TODO: parse args properly
        server.setPort(Integer.parseInt(args[1]));
        server.setApplicationDir(args[3]);
    }

    public void start() throws Exception {
        server.start();
    }
}
