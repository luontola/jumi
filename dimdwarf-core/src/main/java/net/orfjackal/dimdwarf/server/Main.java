// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.server;

import com.google.inject.*;
import net.orfjackal.dimdwarf.modules.CommonModules;
import net.orfjackal.dimdwarf.net.*;
import net.orfjackal.dimdwarf.tasks.TaskExecutor;
import net.orfjackal.dimdwarf.util.MavenUtil;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.*;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        logger.info("Dimdwarf {} starting up", getVersion());

        // TODO: speed up startup by loading classes in parallel
        // Loading the classes is what takes most of the time in startup - on JDK 7 it can be speeded up
        // by loading the classes in parallel. Preliminary tests promise 50% speedup (and 15% slowdown on JDK 6).
        // Doing the following operations in different threads might be able to parallelize the class loading:
        // - create a Guice injector for an empty module (loads Guice's classes)
        // - open a MINA socket acceptor in a random port and close it (loads MINA's classes)
        // - instantiate and run Dimdwarf's modules outside Guice (loads some of Dimdwarf's classes)
        // - create the actual injector with Dimdwarf's modules and return it via a Future (what we really wanted)

        Injector injector = Guice.createInjector(Stage.PRODUCTION, new CommonModules());
        logger.info("Modules loaded");

        injector.getInstance(TaskExecutor.class);
        logger.info("Bootstrapper instantiated");

        // TODO: delegate all the following to a bootsrapper class

        // TODO: parse args properly
        int port = Integer.valueOf(args[1]);
        String applicationDir = args[3];

        // TODO: load the application from the applicationDir

        // TODO: move connection handling to another class
        IoAcceptor acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new SimpleSgsProtocolCodecFactory()));
        acceptor.setHandler(new SimpleSgsProtocolIoHandler());
        acceptor.getSessionConfig().setReadBufferSize(2048);
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);

        logger.info("Begin listening on port {}", port);
        acceptor.bind(new InetSocketAddress(port));

        logger.info("Server started");

        // the server is meant to be crash-only software, so it shall never exit cleanly
        while (true) {
            Thread.sleep(Integer.MAX_VALUE);
        }
    }

    private static String getVersion() {
        String version = MavenUtil.getPom("net.orfjackal.dimdwarf", "dimdwarf-core").getProperty("version");
        return version != null ? version : "<unknown version>";
    }
}
