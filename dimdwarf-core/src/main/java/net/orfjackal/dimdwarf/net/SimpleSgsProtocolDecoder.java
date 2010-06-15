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
 * Decodes {@link com.sun.sgs.protocol.simple.SimpleSgsProtocol} messages.
 */
@Immutable
public class SimpleSgsProtocolDecoder extends CumulativeProtocolDecoder {

    // TODO: ensure compatibility with com.sun.sgs.impl.sharedutil.MessageBuffer

    protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        if (in.prefixedDataAvailable(2, SimpleSgsProtocol.MAX_PAYLOAD_LENGTH)) {
            int payloadLength = in.getUnsignedShort();
            byte op = in.get();

            assert op == SimpleSgsProtocol.LOGIN_REQUEST;
            for (int i = 1; i < payloadLength; i++) {
                in.get();
            }
            Object message = new LoginRequest();

            out.write(message);
            return true;
        } else {
            return false;
        }
    }
}
