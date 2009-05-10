// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.events;

import java.util.EventListener;

/**
 * @author Esko Luontola
 * @since 28.11.2008
 */
public interface SystemLifecycleListener extends EventListener {

    public void onStartup();

    public void onShutdown();
}
