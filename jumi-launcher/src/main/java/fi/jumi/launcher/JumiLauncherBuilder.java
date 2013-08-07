// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher;

import fi.jumi.actors.*;
import fi.jumi.actors.eventizers.dynamic.DynamicEventizerProvider;
import fi.jumi.actors.listeners.*;
import fi.jumi.core.network.*;
import fi.jumi.core.util.PrefixedThreadFactory;
import fi.jumi.launcher.daemon.*;
import fi.jumi.launcher.process.*;
import fi.jumi.launcher.remote.*;
import org.apache.commons.io.output.NullOutputStream;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.OutputStream;
import java.util.concurrent.*;

@NotThreadSafe
public class JumiLauncherBuilder {

    private boolean networkDebugLogging = false;

    public JumiLauncher build() {
        ExecutorService actorsThreadPool = createActorsThreadPool();
        ProcessStarter processStarter = createProcessStarter();
        NetworkServer networkServer = createNetworkServer();
        OutputStream daemonOutputListener = createDaemonOutputListener();

        Actors actors = new MultiThreadedActors(
                actorsThreadPool,
                new DynamicEventizerProvider(),
                new PrintStreamFailureLogger(System.out),
                new NullMessageListener()
        );
        ActorThread actorThread = startActorThread(actors);

        ActorRef<DaemonSummoner> daemonSummoner = actorThread.bindActor(DaemonSummoner.class, new ProcessStartingDaemonSummoner(
                new DirBasedSteward(new EmbeddedDaemonJar()),
                processStarter,
                networkServer,
                daemonOutputListener
        ));
        ActorRef<SuiteLauncher> suiteLauncher = actorThread.bindActor(SuiteLauncher.class, new RemoteSuiteLauncher(actorThread, daemonSummoner));

        return new JumiLauncher(suiteLauncher, () -> {
            networkServer.close();
            actorThread.stop();
            actorsThreadPool.shutdown();
            try {
                actorsThreadPool.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    // configuration parameters

    public JumiLauncherBuilder enableNetworkDebugLogging() {
        networkDebugLogging = true;
        return this;
    }


    // dependencies

    protected ExecutorService createActorsThreadPool() {
        return Executors.newCachedThreadPool(new PrefixedThreadFactory("jumi-launcher-"));
    }

    protected ActorThread startActorThread(Actors actors) {
        return actors.startActorThread(); // in an overridable method for testing purposes
    }

    protected ProcessStarter createProcessStarter() {
        return new SystemProcessStarter();
    }

    protected NetworkServer createNetworkServer() {
        return new NettyNetworkServer(networkDebugLogging);
    }

    protected OutputStream createDaemonOutputListener() {
        return new NullOutputStream();
    }
}
