// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.server;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.auth.*;
import net.orfjackal.dimdwarf.controller.*;
import net.orfjackal.dimdwarf.mq.*;
import net.orfjackal.dimdwarf.net.*;
import net.orfjackal.dimdwarf.services.ServiceRunner;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.*;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ServerStarter {
    private static final Logger logger = LoggerFactory.getLogger(ServerStarter.class);

    private final MessageSender<Object> toController;
    private final ControllerHub controller;
    private final Authenticator authenticator;
    private final SimpleSgsProtocolIoHandler network;

    private int port;
    private String applicationDir;

    @Inject
    public ServerStarter(@Controller MessageSender<Object> toController,
                         ControllerHub controller,
                         Authenticator authenticator,
                         SimpleSgsProtocolIoHandler network) {
        this.toController = toController;
        this.controller = controller;
        this.authenticator = authenticator;
        this.network = network;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setApplicationDir(String applicationDir) {
        this.applicationDir = applicationDir;
    }

    public void start() throws Exception {
        // TODO: load the application from the applicationDir

        // XXX: organize the service wiring and startup

        MessageQueue<Object> toAuthenticator = new MessageQueue<Object>();
        Thread authLoop = new Thread(new ServiceRunner(authenticator, toAuthenticator), "Authenticator");
        authLoop.start();
        AuthenticatorController authenticatorCtrl = new AuthenticatorController(toAuthenticator);
        controller.addService(authenticatorCtrl);

        // TODO: run network in its own thread?
//        MessageQueue<Object> toNetwork = new MessageQueue<Object>();
        bindClientSocket();
        NetworkController networkCtrl = new NetworkController(network, authenticatorCtrl);
        controller.addService(networkCtrl);

        Thread mainLoop = new Thread(new ServiceRunner(controller, (MessageReceiver<Object>) toController), "Controller");
        mainLoop.start();
    }

    private void bindClientSocket() throws IOException {
        // TODO: move connection handling to another class
        IoAcceptor acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new SimpleSgsProtocolCodecFactory()));
        acceptor.setHandler(network);
        acceptor.getSessionConfig().setReadBufferSize(2048);
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);

        logger.info("Begin listening on port {}", port);
        acceptor.bind(new InetSocketAddress(port));
    }
}
