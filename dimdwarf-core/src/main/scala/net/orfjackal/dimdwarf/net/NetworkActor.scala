package net.orfjackal.dimdwarf.net

import com.google.inject.Inject
import java.net.InetSocketAddress
import org.apache.mina.transport.socket.nio.NioSocketAcceptor
import org.apache.mina.filter.codec.ProtocolCodecFilter
import org.apache.mina.core.session.IdleStatus
import org.slf4j.LoggerFactory
import net.orfjackal.dimdwarf.actors._
import com.google.inject.name.Named
import org.apache.mina.filter.logging._

@ActorScoped
class NetworkActor @Inject()(@Named("port") port: Int, ioHandler: SimpleSgsProtocolIoHandler) extends Actor {
  private val logger = LoggerFactory.getLogger(classOf[NetworkActor])
  private val acceptor = createAcceptor

  private def createAcceptor = {
    val acceptor = new NioSocketAcceptor
    acceptor.getFilterChain.addLast("logger", createLoggingFilter)
    acceptor.getFilterChain.addLast("codec", new ProtocolCodecFilter(new SimpleSgsProtocolCodecFactory))
    acceptor.setHandler(ioHandler)
    acceptor.getSessionConfig.setReadBufferSize(2048)
    acceptor.getSessionConfig.setIdleTime(IdleStatus.BOTH_IDLE, 10)
    acceptor
  }

  private def createLoggingFilter = {
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

  def process(message: Any) {
    ioHandler.send(message)
  }
}
