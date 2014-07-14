// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc.encoding;


import fi.jumi.actors.eventizers.Event;
import fi.jumi.core.ipc.api.ResponseListener;
import fi.jumi.core.ipc.buffer.IpcBuffer;

import javax.annotation.concurrent.NotThreadSafe;
import java.nio.file.*;

@NotThreadSafe
public class ResponseListenerEncoding extends EncodingUtil implements ResponseListener, MessageEncoding<ResponseListener> {

    private static final byte onSuiteStarted = 1;

    public ResponseListenerEncoding(IpcBuffer buffer) {
        super(buffer);
    }

    @Override
    public String getInterfaceName() {
        return ResponseListener.class.getName();
    }

    @Override
    public int getInterfaceVersion() {
        return 1;
    }

    @Override
    public void encode(Event<ResponseListener> message) {
        message.fireOn(this);

    }

    @Override
    public void decode(ResponseListener target) {
        byte type = readEventType();
        switch (type) {
            case onSuiteStarted:
                target.onSuiteStarted(readPath());
                break;
            default:
                throw new IllegalArgumentException("Unknown type " + type);
        }
    }


    // encoding events

    @Override
    public void onSuiteStarted(Path suiteResults) {
        writeEventType(onSuiteStarted);
        writePath(suiteResults);
    }


    // Path

    private void writePath(Path path) {
        writeString(path.toString());
    }

    private Path readPath() {
        return Paths.get(readString());
    }
}
