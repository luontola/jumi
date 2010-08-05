package net.orfjackal.dimdwarf.net

import org.apache.mina.core.buffer.IoBuffer
import com.sun.sgs.protocol.simple.SimpleSgsProtocol
import org.apache.mina.filter.codec._
import org.apache.mina.core.session.IoSession
import javax.annotation.concurrent.Immutable

@Immutable
class SimpleSgsProtocolDecoder extends CumulativeProtocolDecoder {
  protected def doDecode(session: IoSession, in: IoBuffer, out: ProtocolDecoderOutput): Boolean = {
    if (in.prefixedDataAvailable(2, SimpleSgsProtocol.MAX_PAYLOAD_LENGTH)) {
      val payloadLength = in.getUnsignedShort()
      val op = in.get()

      assert(op == SimpleSgsProtocol.LOGIN_REQUEST)
      for (i <- 1 until payloadLength) {
        in.get()
      }

      out.write(LoginRequest())
      true
    } else {
      false
    }
  }
}
