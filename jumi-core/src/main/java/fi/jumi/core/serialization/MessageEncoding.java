// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.serialization;

import fi.jumi.actors.eventizers.Event;

public interface MessageEncoding<T> {

    String getInterfaceName();

    int getInterfaceVersion();

    void encode(Event<T> message);

    void decode(T target);
}
