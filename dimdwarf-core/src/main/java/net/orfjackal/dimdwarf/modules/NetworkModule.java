// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.modules;

import com.google.inject.Provides;
import com.google.inject.name.Names;
import net.orfjackal.dimdwarf.mq.MessageReceiver;
import net.orfjackal.dimdwarf.net.*;
import net.orfjackal.dimdwarf.services.*;

public class NetworkModule extends ServiceModule {

    private final int port;

    public NetworkModule(int port) {
        super("Network");
        this.port = port;
    }

    protected void configure() {
        bindConstant().annotatedWith(Names.named("port")).to(port);

        bindControllerTo(NetworkController.class);
        bindServiceTo(NetworkService.class);
        bindMessageQueueOfType(Object.class);
    }

    @Provides
    ServiceRunnable service(Service service, MessageReceiver<Object> toService) {
        return new ServiceMessageLoop(service, toService);
    }
}
