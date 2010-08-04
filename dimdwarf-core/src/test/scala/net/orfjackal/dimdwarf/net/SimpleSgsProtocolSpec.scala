package net.orfjackal.dimdwarf.net

import org.junit.Assert._
import org.hamcrest.CoreMatchers._
import org.junit.runner.RunWith
import net.orfjackal.specsy._
import org.apache.mina.core.session._
import org.apache.mina.core.buffer._
import org.mockito.Mockito._
import com.sun.sgs.protocol.simple.SimpleSgsProtocol
import com.sun.sgs.impl.sharedutil.MessageBuffer
import org.apache.mina.filter.codec._

@RunWith(classOf[Specsy])
class SimpleSgsProtocolSpec extends Spec {
  val session = new DummySession
  val decoded = mock(classOf[ProtocolDecoderOutput])
  val encoded = mock(classOf[ProtocolEncoderOutput])

  val encoder = new SimpleSgsProtocolEncoder
  val decoder = new SimpleSgsProtocolDecoder

  assertThat(true, is(true)) // TODO (this line is here just to keep the imports in place)

  "Decode LOGIN_REQUEST" >> {
    val in = loginRequest("username", "password")

    decoder.decode(session, in, decoded)

    verify(decoded).write(LoginRequest()) // TODO: add username and password
  }

  "Encode LOGIN_FAILURE" >> {
    val message = LoginFailure()

    encoder.encode(session, message, encoded)

    verify(encoded).write(loginFailure("")) // TODO: add reason
  }

  def loginRequest(user: String, pass: String): IoBuffer = {
    val length = 2 +
            MessageBuffer.getSize(user) +
            MessageBuffer.getSize(pass)
    val message = new MessageBuffer(length).
            putByte(SimpleSgsProtocol.LOGIN_REQUEST).
            putByte(SimpleSgsProtocol.VERSION).
            putString(user).
            putString(pass)
    asIoBuffer(message.getBuffer)
  }

  def loginFailure(reason: String): IoBuffer = {
    val length = 1 +
            MessageBuffer.getSize(reason)
    val message = new MessageBuffer(length).
            putByte(SimpleSgsProtocol.LOGIN_FAILURE).
            putString(reason)
    asIoBuffer(message.getBuffer)
  }

  private def asIoBuffer(bytes: Array[Byte]): IoBuffer = {
    IoBuffer.allocate(2 + bytes.length).
            putShort(bytes.length.asInstanceOf[Short]).
            put(bytes).
            flip()
  }
}
