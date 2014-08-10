// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import fi.jumi.actors.*;
import fi.jumi.core.api.SuiteListener;
import fi.jumi.core.config.SuiteConfiguration;
import fi.jumi.core.events.*;
import fi.jumi.core.events.suiteListener.OnSuiteFinishedEvent;
import fi.jumi.core.ipc.api.*;
import fi.jumi.core.ipc.channel.*;
import fi.jumi.core.ipc.dirs.*;
import fi.jumi.core.ipc.encoding.*;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.nio.file.Path;

@NotThreadSafe
public class IpcCommandReceiver implements Runnable {

    private final DaemonDir daemonDir;
    private final CommandDir commandDir;
    private final CommandListener commandListener;
    private final ActorThread actorThread;

    public IpcCommandReceiver(DaemonDir daemonDir, CommandDir commandDir, CommandListener commandListener, ActorThread actorThread) {
        this.daemonDir = daemonDir;
        this.commandDir = commandDir;
        this.commandListener = commandListener;
        this.actorThread = actorThread;
    }

    @Override
    public void run() {
        IpcReader<RequestListener> requestReader = IpcChannel.reader(commandDir.getRequestPath(), RequestListenerEncoding::new);
        ActorRef<RequestHandler> requestHandler = actorThread.bindActor(RequestHandler.class,
                new RequestHandlerImpl(daemonDir, commandDir, commandListener, actorThread));
        requestHandler.tell().start();
        try {
            IpcReaders.decodeAll(requestReader, requestHandler.tell());
        } catch (InterruptedException e) {
            System.err.println(this + " interrupted");
            Thread.currentThread().interrupt();
        }
        requestHandler.tell().finish();
    }


    @NotThreadSafe
    private static class RequestHandlerImpl implements RequestHandler {

        private final DaemonDir daemonDir;
        private final CommandDir commandDir;
        private final CommandListener commandListener;
        private final ActorThread actorThread;

        private IpcWriter<ResponseListener> responseWriter;
        private ResponseListener response;

        public RequestHandlerImpl(DaemonDir daemonDir, CommandDir commandDir, CommandListener commandListener, ActorThread actorThread) {
            this.daemonDir = daemonDir;
            this.commandDir = commandDir;
            this.commandListener = commandListener;
            this.actorThread = actorThread;
        }

        @Override
        public void start() {
            this.responseWriter = IpcChannel.writer(commandDir.getResponsePath(), ResponseListenerEncoding::new);
            this.response = new ResponseListenerEventizer().newFrontend(responseWriter);
        }

        @Override
        public void finish() {
            responseWriter.close();
        }

        @Override
        public void runTests(SuiteConfiguration suiteConfiguration) {
            Path suiteResults = newSuiteResultsFile();
            ActorRef<SuiteListener> suiteWriter = startSuiteWriter(suiteResults);
            response.onSuiteStarted(suiteResults);
            commandListener.runTests(suiteConfiguration, suiteWriter);
        }

        private Path newSuiteResultsFile() {
            try {
                return daemonDir.createSuiteDir().getSuiteResultsPath();
            } catch (IOException e) {
                // TODO: write a failure to results file?
                throw new RuntimeException(e);
            }
        }

        private ActorRef<SuiteListener> startSuiteWriter(Path suiteResults) {
            IpcWriter<SuiteListener> suiteWriter = IpcChannel.writer(suiteResults, SuiteListenerEncoding::new);
            SuiteListener frontend = new SuiteListenerEventizer().newFrontend(message -> {
                suiteWriter.send(message);
                if (message instanceof OnSuiteFinishedEvent) { // XXX
                    suiteWriter.close();
                }
            });
            return actorThread.bindActor(SuiteListener.class, frontend);
        }

        @Override
        public void shutdown() {
            commandListener.shutdown();
        }

        @Override
        public String toString() {
            return getClass().getName() + "(" + commandDir + ")";
        }
    }
}
