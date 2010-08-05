package net.orfjackal.dimdwarf.net

import org.apache.mina.core.service.IoHandlerAdapter
import org.apache.mina.core.session.IoSession
import org.slf4j._
import SimpleSgsProtocolIoHandler._

object SimpleSgsProtocolIoHandler {
  private val logger = LoggerFactory.getLogger(classOf[SimpleSgsProtocolIoHandler])
}

class SimpleSgsProtocolIoHandler extends IoHandlerAdapter {
  override def messageReceived(session: IoSession, message: Any) {
    logger.debug("Message received: {}", message)
    if (message.isInstanceOf[LoginRequest]) {
      session.write(LoginFailure())
    }
  }
}
