// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.actors;

import com.google.inject.Injector;
import net.orfjackal.dimdwarf.context.BaseContext;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;

@NotThreadSafe
public class ActorContext extends BaseContext {

    @Inject
    public ActorContext(Injector injector) {
        super(injector);
    }
}
