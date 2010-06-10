// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.net;

import com.sun.sgs.protocol.simple.SimpleSgsProtocol;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

/**
 * Handler for the {@link com.sun.sgs.protocol.simple.SimpleSgsProtocol} server-side protocol.
 */
public class SimpleSgsProtocolIoHandler extends IoHandlerAdapter {

    public void messageReceived(IoSession session, Object message) throws Exception {
//        System.out.println("session = " + session);
//        System.out.println("message = " + message);

//        MessageBuffer response = new MessageBuffer(SimpleSgsProtocol.MAX_MESSAGE_LENGTH);
//        response.putShort(999);   // payload length (excluding these bytes)
//        response.putByte(SimpleSgsProtocol.LOGIN_FAILURE);
//        response.putString("wrong password");
//        byte[] buf = response.getBuffer();

        IoBuffer buffer = IoBuffer.allocate(SimpleSgsProtocol.MAX_MESSAGE_LENGTH);
        buffer.putShort((short) 3); // payload length
        buffer.put(SimpleSgsProtocol.LOGIN_FAILURE);
        buffer.putShort((short) 0); // (String) message for login failure (length = 0)
        buffer.flip();

        session.write(buffer);
    }
}
