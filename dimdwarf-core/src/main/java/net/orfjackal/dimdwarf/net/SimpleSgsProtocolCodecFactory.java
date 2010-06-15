// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.net;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.*;

public class SimpleSgsProtocolCodecFactory implements ProtocolCodecFactory {

    public ProtocolEncoder getEncoder(IoSession session) throws Exception {
        return new SimpleSgsProtocolEncoder();
    }

    public ProtocolDecoder getDecoder(IoSession session) throws Exception {
        return new SimpleSgsProtocolDecoder();
    }
}
