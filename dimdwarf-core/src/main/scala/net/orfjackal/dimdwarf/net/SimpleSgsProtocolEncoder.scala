package net.orfjackal.dimdwarf.net

import com.sun.sgs.protocol.simple.SimpleSgsProtocol
import org.apache.mina.filter.codec._
import org.apache.mina.core.session.IoSession
import org.apache.mina.core.buffer.IoBuffer
import javax.annotation.concurrent.Immutable

@Immutable
class SimpleSgsProtocolEncoder extends ProtocolEncoder {
  def encode(session: IoSession, message: Any, out: ProtocolEncoderOutput) {
    if (message.isInstanceOf[LoginFailure]) {
      val buffer = IoBuffer.allocate(5).
              putShort(3.asInstanceOf[Short]).
              put(SimpleSgsProtocol.LOGIN_FAILURE).
              putShort(0.asInstanceOf[Short]).
              flip()
      out.write(buffer)
    }
  }

  def dispose(session: IoSession) {
  }
}
