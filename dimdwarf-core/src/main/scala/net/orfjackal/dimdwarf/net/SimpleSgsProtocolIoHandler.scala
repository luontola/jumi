package net.orfjackal.dimdwarf.net

import org.apache.mina.core.service.IoHandlerAdapter
import org.apache.mina.core.session.IoSession
import org.slf4j._
import SimpleSgsProtocolIoHandler._
import net.orfjackal.dimdwarf.mq.MessageSender
import net.orfjackal.dimdwarf.controller.Hub
import com.google.inject._

object SimpleSgsProtocolIoHandler {
  private val logger = LoggerFactory.getLogger(classOf[SimpleSgsProtocolIoHandler])
}

@Singleton
class SimpleSgsProtocolIoHandler @Inject()(@Hub toHub: MessageSender[Any]) extends IoHandlerAdapter with MessageSender[Any] {
  // XXX: support multiple clients, give an ID for each session
  private var lastSession: IoSession = null

  def send(message: Any) {
    if (lastSession != null) {
      lastSession.write(message)
    }
  }

  override def messageReceived(session: IoSession, message: Any) {
    logger.debug("RECEIVED: {}", message)

    lastSession = session
    toHub.send(message)
  }
}
