// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.daemon;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.MessageSender;
import fi.jumi.core.api.SuiteListener;
import fi.jumi.core.config.SuiteConfiguration;
import fi.jumi.core.events.SuiteListenerEventizer;
import fi.jumi.core.ipc.IpcCommandSender;
import fi.jumi.core.ipc.api.RequestListener;
import fi.jumi.core.ipc.channel.*;
import fi.jumi.core.ipc.dirs.*;
import fi.jumi.core.ipc.encoding.SuiteListenerEncoding;
import fi.jumi.core.network.*;
import fi.jumi.core.suite.SuiteFactory;
import fi.jumi.core.util.timeout.*;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.*;

@ThreadSafe
public class DaemonNetworkEndpoint implements NetworkEndpoint<Event<RequestListener>, Event<SuiteListener>>, RequestListener {

    // TODO: remove this class

    private final SuiteFactory suiteFactory;
    private final Runnable shutdownHook;
    private final Timeout startupTimeout;
    private final DaemonDir daemonDir;
    private final VacancyTimeout connections;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private MessageSender<Event<SuiteListener>> sender;

    public DaemonNetworkEndpoint(SuiteFactory suiteFactory, Runnable shutdownHook, Timeout startupTimeout, Timeout idleTimeout, DaemonDir daemonDir) {
        this.suiteFactory = suiteFactory;
        this.shutdownHook = shutdownHook;
        this.startupTimeout = startupTimeout;
        this.daemonDir = daemonDir;
        this.connections = new VacancyTimeout(idleTimeout);
    }

    @Override
    public void onConnected(NetworkConnection connection, MessageSender<Event<SuiteListener>> sender) {
        this.sender = sender;
        startupTimeout.cancel();
        connections.checkIn();
    }

    @Override
    public void onDisconnected() {
        connections.checkOut();
    }

    @Override
    public void onMessage(Event<RequestListener> message) {
        message.fireOn(this);
    }

    @Override
    public void runTests(SuiteConfiguration suite) {
        SuiteListener suiteListener = new SuiteListenerEventizer().newFrontend(sender);
//        suiteFactory.configure(suite);
//        suiteFactory.start(suiteListener);

        // XXX: routing the commands through IPC to make sure that IPC works
        try {
            CommandDir commandDir = daemonDir.createCommandDir();
            IpcCommandSender sender = new IpcCommandSender(commandDir, executor);
            Future<Path> suiteResults = sender.runTests(suite);
            sender.close();

            executor.execute(() -> {
                try {
                    IpcReader<SuiteListener> suiteReader = IpcChannel.reader(suiteResults.get(), SuiteListenerEncoding::new);
                    IpcReaders.decodeAll(suiteReader, suiteListener);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void shutdown() {
//        shutdownHook.run();

        // XXX: routing the commands through IPC to make sure that IPC works
        try {
            CommandDir commandDir = daemonDir.createCommandDir();
            IpcCommandSender sender = new IpcCommandSender(commandDir, executor);
            sender.shutdown();
            sender.close();

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
