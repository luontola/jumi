package net.orfjackal.dimdwarf.net

import org.apache.mina.core.buffer.IoBuffer
import com.sun.sgs.protocol.simple.SimpleSgsProtocol
import org.apache.mina.filter.codec._
import org.apache.mina.core.session.IoSession
import javax.annotation.concurrent.Immutable
import java.nio.charset.Charset
import com.sun.sgs.impl.sharedutil.MessageBuffer

@Immutable
class SimpleSgsProtocolDecoder extends CumulativeProtocolDecoder {
  private val stringCharset = Charset.forName("UTF-8")

  protected def doDecode(session: IoSession, in: IoBuffer, out: ProtocolDecoderOutput): Boolean = {
    if (in.prefixedDataAvailable(2, SimpleSgsProtocol.MAX_PAYLOAD_LENGTH)) {
      val payloadLength = in.getUnsignedShort()
      val op = in.get()

      val message = op match {
        case SimpleSgsProtocol.LOGIN_REQUEST =>
          val version = in.get()
          require(version == SimpleSgsProtocol.VERSION, "incompatible version: " + version)
          val username = readString(in)
          val password = readString(in)
          LoginRequest(username, password)
      }

      out.write(message)
      true
    } else {
      false
    }
  }

  private def readString(in: IoBuffer): String = {
    val length = in.getUnsignedShort()
    val bytes = new Array[Byte](length)
    in.get(bytes)

    val buf = new MessageBuffer(2 + length).
            putShort(length).
            putBytes(bytes)
    buf.rewind()
    buf.getString()
  }
}
