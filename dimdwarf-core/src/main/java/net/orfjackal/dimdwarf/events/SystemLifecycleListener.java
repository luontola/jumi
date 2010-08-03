// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.events;

import java.util.EventListener;

public interface SystemLifecycleListener extends EventListener {

    // FIXME: SystemLifecycleListener will be removed/refactored in new architecture

    public void onStartup();

    public void onShutdown();
}
