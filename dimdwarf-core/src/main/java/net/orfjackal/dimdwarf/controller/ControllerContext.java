// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.controller;

import com.google.inject.Injector;
import net.orfjackal.dimdwarf.context.BaseContext;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;

@NotThreadSafe
public class ControllerContext extends BaseContext {

    @Inject
    public ControllerContext(Injector injector) {
        super(injector);
    }
}
