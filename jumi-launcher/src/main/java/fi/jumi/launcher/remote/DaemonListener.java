// Copyright © 2011-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.remote;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.generator.GenerateEventizer;
import fi.jumi.actors.queue.MessageSender;
import fi.jumi.core.api.SuiteListener;
import fi.jumi.core.ipc.api.RequestListener;
import fi.jumi.core.network.*;

@GenerateEventizer(targetPackage = "fi.jumi.launcher.events")
public interface DaemonListener extends NetworkEndpoint<Event<SuiteListener>, Event<RequestListener>> {

    // XXX: overrides needed due to jumi-actors-generator not yet supporting inheritance

    @Override
    void onConnected(NetworkConnection connection, MessageSender<Event<RequestListener>> sender);

    @Override
    void onDisconnected();

    @Override
    void onMessage(Event<SuiteListener> message);
}
