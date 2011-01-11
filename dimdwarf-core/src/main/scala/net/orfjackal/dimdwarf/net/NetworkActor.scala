package net.orfjackal.dimdwarf.net

import java.net.InetSocketAddress
import org.apache.mina.transport.socket.nio.NioSocketAcceptor
import org.apache.mina.filter.codec.ProtocolCodecFilter
import org.slf4j.LoggerFactory
import net.orfjackal.dimdwarf.actors._
import org.apache.mina.filter.logging._
import net.orfjackal.dimdwarf.mq.MessageSender
import org.apache.mina.core.service.IoHandlerAdapter
import org.apache.mina.core.session._
import net.orfjackal.dimdwarf.controller.Hub
import net.orfjackal.dimdwarf.net.sgs._
import javax.inject._

@ActorScoped
class NetworkActor @Inject()(@Named("port") port: Int, @Hub toHub: MessageSender[Any]) extends IoHandlerAdapter with Actor[NetworkMessage] {
  private val logger = LoggerFactory.getLogger(classOf[NetworkActor])
  private val acceptor = createAcceptor()

  private def createAcceptor() = {
    val acceptor = new NioSocketAcceptor
    acceptor.getFilterChain.addLast("logger", createLoggingFilter())
    acceptor.getFilterChain.addLast("codec", new ProtocolCodecFilter(new SimpleSgsProtocolCodecFactory))
    acceptor.setHandler(this)
    acceptor.getSessionConfig.setReadBufferSize(2048)
    acceptor.getSessionConfig.setIdleTime(IdleStatus.BOTH_IDLE, 10)
    acceptor
  }

  private def createLoggingFilter() = {
    val filter = new LoggingFilter(getClass)
    filter.setSessionCreatedLogLevel(LogLevel.DEBUG)
    filter.setSessionOpenedLogLevel(LogLevel.DEBUG)
    filter.setSessionClosedLogLevel(LogLevel.DEBUG)
    filter.setSessionIdleLogLevel(LogLevel.DEBUG)
    filter.setMessageReceivedLogLevel(LogLevel.DEBUG)
    filter.setMessageSentLogLevel(LogLevel.DEBUG)
    filter
  }

  def start() {
    logger.info("Begin listening on port {}", port)
    acceptor.bind(new InetSocketAddress(port))
  }

  def stop() {
    acceptor.unbind()
  }

  def process(message: NetworkMessage) {
    message match {
      case SendToClient(message, IoSessionHandle(session)) =>
        session.write(message)
    }
  }

  override def messageReceived(session: IoSession, message: Any) {
    logger.debug("RECEIVED: {}", message)

    forwardToController(message.asInstanceOf[ClientMessage], session)
  }

  private def forwardToController(message: ClientMessage, session: IoSession) {
    toHub.send(
      ReceivedFromClient(message, IoSessionHandle(session)))
  }

  private case class IoSessionHandle(session: IoSession) extends SessionHandle
}
