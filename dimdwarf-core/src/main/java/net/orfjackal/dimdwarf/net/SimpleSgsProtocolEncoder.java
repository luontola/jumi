// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.net;

import com.sun.sgs.protocol.simple.SimpleSgsProtocol;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.*;

import javax.annotation.concurrent.Immutable;

/**
 * Encodes {@link com.sun.sgs.protocol.simple.SimpleSgsProtocol} messages.
 */
@Immutable
public class SimpleSgsProtocolEncoder implements ProtocolEncoder {

    // TODO: ensure compatibility with com.sun.sgs.impl.sharedutil.MessageBuffer

    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        if (message instanceof LoginFailure) {
            IoBuffer buffer = IoBuffer.allocate(5);
            buffer.putShort((short) 3); // payload length
            buffer.put(SimpleSgsProtocol.LOGIN_FAILURE);
            buffer.putShort((short) 0); // (String) message for login failure (length = 0)
            buffer.flip();
            out.write(buffer);
        }
    }

    public void dispose(IoSession session) throws Exception {
    }
}
