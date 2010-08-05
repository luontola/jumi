package net.orfjackal.dimdwarf.net

import org.apache.mina.core.service.IoHandlerAdapter
import org.apache.mina.core.session.IoSession
import org.slf4j._
import SimpleSgsProtocolIoHandler._
import net.orfjackal.dimdwarf.mq.MessageSender
import net.orfjackal.dimdwarf.controller.RegisterNetworkService

object SimpleSgsProtocolIoHandler {
  private val logger = LoggerFactory.getLogger(classOf[SimpleSgsProtocolIoHandler])
}

class SimpleSgsProtocolIoHandler(toController: MessageSender[Any]) extends IoHandlerAdapter with MessageSender[Any] {
  private var lastSession: IoSession = null // TODO: support multiple clients, give an ID for each session

  toController.send(RegisterNetworkService(this))

  def send(message: Any) {
    if (lastSession != null) {
      lastSession.write(message)
    }
  }

  override def messageReceived(session: IoSession, message: Any) {
    logger.debug("Message received: {}", message)

    lastSession = session
    toController.send(message)
  }
}
