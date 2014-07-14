// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import fi.jumi.core.events.CommandListenerEventizer;
import fi.jumi.core.ipc.api.CommandListener;
import fi.jumi.core.ipc.channel.*;
import fi.jumi.core.ipc.dirs.CommandDir;
import fi.jumi.core.ipc.encoding.CommandListenerEncoding;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.Closeable;

@NotThreadSafe
public class IpcCommandWriter implements Closeable {

    private final IpcWriter<CommandListener> writer;
    private final CommandListener target;

    public IpcCommandWriter(CommandDir dir) {
        writer = IpcChannel.writer(dir.getRequestPath(), CommandListenerEncoding::new);
        target = new CommandListenerEventizer().newFrontend(writer);
    }

    public CommandListener tell() {
        return target;
    }

    @Override
    public void close() {
        writer.close();
    }
}
