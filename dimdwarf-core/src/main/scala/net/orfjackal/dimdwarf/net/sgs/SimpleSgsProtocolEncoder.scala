package net.orfjackal.dimdwarf.net.sgs

import com.sun.sgs.protocol.simple.SimpleSgsProtocol
import org.apache.mina.filter.codec._
import org.apache.mina.core.session.IoSession
import org.apache.mina.core.buffer.IoBuffer
import javax.annotation.concurrent.Immutable

@Immutable
class SimpleSgsProtocolEncoder extends ProtocolEncoderAdapter {
  def encode(session: IoSession, message: Any, out: ProtocolEncoderOutput) {
    val encoded = message match {
    // TODO: calculate the length of the messages dynamically?

      case LoginSuccess() =>
        IoBuffer.allocate(5).
                putShort(3.asInstanceOf[Short]). // message length
                put(SimpleSgsProtocol.LOGIN_SUCCESS). // op code
                putShort(0.asInstanceOf[Short]). // reconnectionKey
                flip()

      case LoginFailure() =>
        IoBuffer.allocate(5).
                putShort(3.asInstanceOf[Short]). // message length
                put(SimpleSgsProtocol.LOGIN_FAILURE). // op code
                putShort(0.asInstanceOf[Short]). // reason
                flip()

      case LogoutSuccess() =>
        IoBuffer.allocate(3).
                putShort(1.asInstanceOf[Short]). // message length
                put(SimpleSgsProtocol.LOGOUT_SUCCESS). // op code
                flip()
    }
    out.write(encoded)
  }
}
