// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.remote;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.core.api.SuiteListener;
import fi.jumi.core.ipc.api.CommandListener;
import fi.jumi.core.network.NetworkEndpoint;

public interface DaemonListener extends NetworkEndpoint<Event<SuiteListener>, Event<CommandListener>> {
}
