// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.server;

import net.orfjackal.dimdwarf.net.SimpleSgsProtocolIoHandler;
import net.orfjackal.dimdwarf.util.MavenUtil;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.*;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        logger.info("Dimdwarf {} starting up", getVersion());

        // TODO: start up server etc.
        // Guice.createInjector(Stage.PRODUCTION, ...)

        // TODO: parse args properly
        int port = Integer.valueOf(args[1]);
        String applicationDir = args[3];

        // TODO: load the application from the applicationDir

        // TODO: move connection handling to another class
        IoAcceptor acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        acceptor.setHandler(new SimpleSgsProtocolIoHandler());
        acceptor.getSessionConfig().setReadBufferSize(2048);
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);

        logger.info("Begin listening on port " + port);
        acceptor.bind(new InetSocketAddress(port));

        // TODO: wait for the application to exit (or actually, wait indefinitely because it's meant to be crash-only software)
        Thread.sleep(1000);

        logger.info("Shutting down");
        acceptor.unbind();
    }

    private static String getVersion() {
        String version = MavenUtil.getPom("net.orfjackal.dimdwarf", "dimdwarf-core").getProperty("version");
        return version != null ? version : "<unknown version>";
    }
}
