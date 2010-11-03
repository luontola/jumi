package net.orfjackal.dimdwarf.net.sgs

import org.apache.mina.core.buffer.IoBuffer
import com.sun.sgs.impl.sharedutil.MessageBuffer
import com.sun.sgs.protocol.simple.SimpleSgsProtocol
import java.io._

object SimpleSgsProtocolReferenceMessages {
  def nextMessage(input: InputStream): IoBuffer = {
    val in = new DataInputStream(input)
    val length = in.readUnsignedShort
    val message = new Array[Byte](length)
    in.readFully(message)
    asIoBuffer(message)
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

  def loginSuccess(reconnectionKey: Array[Byte]): IoBuffer = {
    val length = 1 +
            2 + reconnectionKey.length
    val message = new MessageBuffer(length).
            putByte(SimpleSgsProtocol.LOGIN_SUCCESS).
            putByteArray(reconnectionKey)
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

  def logoutRequest(): IoBuffer = {
    asIoBuffer(Array[Byte](SimpleSgsProtocol.LOGOUT_REQUEST))
  }

  def logoutSuccess(): IoBuffer = {
    asIoBuffer(Array[Byte](SimpleSgsProtocol.LOGOUT_SUCCESS))
  }

  private def asIoBuffer(bytes: Array[Byte]): IoBuffer = {
    IoBuffer.allocate(2 + bytes.length).
            putShort(bytes.length.asInstanceOf[Short]).
            put(bytes).
            flip()
  }
}
