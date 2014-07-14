// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import fi.jumi.core.events.RequestListenerEventizer;
import fi.jumi.core.ipc.api.RequestListener;
import fi.jumi.core.ipc.channel.*;
import fi.jumi.core.ipc.dirs.CommandDir;
import fi.jumi.core.ipc.encoding.RequestListenerEncoding;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.Closeable;

@NotThreadSafe
public class IpcCommandWriter implements Closeable {

    private final IpcWriter<RequestListener> writer;
    private final RequestListener target;

    public IpcCommandWriter(CommandDir dir) {
        writer = IpcChannel.writer(dir.getRequestPath(), RequestListenerEncoding::new);
        target = new RequestListenerEventizer().newFrontend(writer);
    }

    public RequestListener tell() {
        return target;
    }

    @Override
    public void close() {
        writer.close();
    }
}
