package net.orfjackal.dimdwarf.net

import com.sun.sgs.protocol.simple.SimpleSgsProtocol
import org.apache.mina.filter.codec._
import org.apache.mina.core.session.IoSession
import org.apache.mina.core.buffer.IoBuffer
import javax.annotation.concurrent.Immutable

@Immutable
class SimpleSgsProtocolEncoder extends ProtocolEncoder {
  def encode(session: IoSession, message: Any, out: ProtocolEncoderOutput) {
    val encoded = message match {

      case LoginSuccess() =>
        IoBuffer.allocate(5).
                putShort(3.asInstanceOf[Short]).
                put(SimpleSgsProtocol.LOGIN_SUCCESS).
                putShort(0.asInstanceOf[Short]).
                flip()

      case LoginFailure() =>
        IoBuffer.allocate(5).
                putShort(3.asInstanceOf[Short]).
                put(SimpleSgsProtocol.LOGIN_FAILURE).
                putShort(0.asInstanceOf[Short]).
                flip()

      case LogoutSuccess() =>
        IoBuffer.allocate(3).
                putShort(1.asInstanceOf[Short]).
                put(SimpleSgsProtocol.LOGIN_SUCCESS).
                flip()
    }
    out.write(encoded)
  }

  def dispose(session: IoSession) {
  }
}
