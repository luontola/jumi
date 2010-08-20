// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.modules;

import com.google.inject.*;
import net.orfjackal.dimdwarf.controller.Hub;
import net.orfjackal.dimdwarf.mq.*;

public class ControllerModule extends AbstractModule {

    protected void configure() {
        bind(new TypeLiteral<MessageSender<Object>>() {}).annotatedWith(Hub.class).toInstance(new MessageQueue<Object>());
    }
}
