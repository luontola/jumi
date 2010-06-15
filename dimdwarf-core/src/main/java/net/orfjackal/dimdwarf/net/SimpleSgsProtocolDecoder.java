// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.net;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.*;

public class SimpleSgsProtocolDecoder extends CumulativeProtocolDecoder {

    // TODO: ensure compatibility with com.sun.sgs.impl.sharedutil.MessageBuffer

    protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {

        // "doDecode() can't return true when buffer is not consumed."
        for (int i = 0; i < in.limit(); i++) {
            in.get();
        }

        out.write(new LoginRequest());
        return true;
    }
}
