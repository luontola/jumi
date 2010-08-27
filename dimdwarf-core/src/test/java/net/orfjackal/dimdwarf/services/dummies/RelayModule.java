// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.services.dummies;

import com.google.inject.Provides;
import net.orfjackal.dimdwarf.mq.MessageReceiver;
import net.orfjackal.dimdwarf.services.*;

public class RelayModule extends ServiceModule {

    public RelayModule() {
        super("Relay");
    }

    protected void configure() {
        bindControllerTo(RelayController.class);
        bindServiceTo(RelayService.class);
        bindMessageQueueOfType(Object.class);
    }

    @Provides
    ServiceRunnable service(Service service, MessageReceiver<Object> toService) {
        return new ServiceMessageLoop(service, toService);
    }
}
