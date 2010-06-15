// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.net;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.*;

public class SimpleSgsProtocolIoHandler extends IoHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SimpleSgsProtocolIoHandler.class);

    public void messageReceived(IoSession session, Object message) throws Exception {
        logger.debug("Message received: {}", message);
        if (message instanceof LoginRequest) {
            session.write(new LoginFailure());
        }
    }
}
