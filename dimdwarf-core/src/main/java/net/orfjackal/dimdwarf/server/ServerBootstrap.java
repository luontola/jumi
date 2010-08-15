// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.server;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.controller.Controller;
import net.orfjackal.dimdwarf.mq.MessageQueue;
import net.orfjackal.dimdwarf.net.*;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.*;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ServerBootstrap {
    private static final Logger logger = LoggerFactory.getLogger(ServerBootstrap.class);

    private final Controller controller;
    private MessageQueue<Object> toController = new MessageQueue<Object>(); // TODO: organize the message queues somehow

    private int port;
    private String applicationDir;

    @Inject
    public ServerBootstrap(Controller controller) {
        this.controller = controller;
    }

    public void configure(String[] args) {
        // TODO: parse args properly
        port = Integer.parseInt(args[1]);
        applicationDir = args[3];
    }

    public void start() throws IOException {
        // TODO: load the application from the applicationDir

        bindClientSocket();

        Thread mainLoop = new Thread(new ControllerMainLoop(controller, toController), "Controller");
        mainLoop.start();
    }

    private void bindClientSocket() throws IOException {
        // TODO: move connection handling to another class
        IoAcceptor acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new SimpleSgsProtocolCodecFactory()));
        acceptor.setHandler(new SimpleSgsProtocolIoHandler(toController));
        acceptor.getSessionConfig().setReadBufferSize(2048);
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);

        logger.info("Begin listening on port {}", port);
        acceptor.bind(new InetSocketAddress(port));
    }
}
