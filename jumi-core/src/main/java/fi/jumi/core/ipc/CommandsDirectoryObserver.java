// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import fi.jumi.actors.ActorThread;
import fi.jumi.core.ipc.api.CommandListener;
import fi.jumi.core.ipc.dirs.*;

import javax.annotation.concurrent.NotThreadSafe;
import java.nio.file.Path;
import java.util.concurrent.Executor;

@NotThreadSafe
public class CommandsDirectoryObserver implements Runnable {

    private final DirectoryObserver directoryObserver;

    public CommandsDirectoryObserver(DaemonDir daemonDir, Executor executor, ActorThread actorThread, CommandListener commandListener) {
        directoryObserver = new DirectoryObserver(daemonDir.getCommandsDir(), new DirectoryObserver.Listener() {
            @Override
            public void onFileNoticed(Path path) {
                executor.execute(new IpcCommandReceiver(daemonDir, new CommandDir(path), commandListener, actorThread));
            }
        });
    }

    @Override
    public void run() {
        directoryObserver.run();
    }
}
