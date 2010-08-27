// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.server;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.services.ServiceStarter;

public class ServerStarter {

    private final ServiceStarter services;

    public static int port; // XXX: use a better way to pass the parameters
    private String applicationDir;

    @Inject
    public ServerStarter(ServiceStarter services) {
        this.services = services;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setApplicationDir(String applicationDir) {
        this.applicationDir = applicationDir;
    }

    public void start() throws Exception {
        // TODO: load the application from the applicationDir

        services.start();
    }
}
