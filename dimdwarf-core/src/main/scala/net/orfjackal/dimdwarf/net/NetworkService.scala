package net.orfjackal.dimdwarf.net

import com.google.inject.Inject
import java.net.InetSocketAddress
import org.apache.mina.transport.socket.nio.NioSocketAcceptor
import org.apache.mina.filter.logging.LoggingFilter
import org.apache.mina.filter.codec.ProtocolCodecFilter
import org.apache.mina.core.session.IdleStatus
import org.slf4j.LoggerFactory
import net.orfjackal.dimdwarf.server.ServerStarter
import net.orfjackal.dimdwarf.services._

@ServiceScoped
class NetworkService @Inject()(ioHandler: SimpleSgsProtocolIoHandler) extends Service {
  private val logger = LoggerFactory.getLogger(classOf[NetworkService])

  def start() {
    bindClientSocket()
  }

  private def bindClientSocket() {
    val acceptor = new NioSocketAcceptor
    acceptor.getFilterChain.addLast("logger", new LoggingFilter)
    acceptor.getFilterChain.addLast("codec", new ProtocolCodecFilter(new SimpleSgsProtocolCodecFactory))
    acceptor.setHandler(ioHandler)
    acceptor.getSessionConfig.setReadBufferSize(2048)
    acceptor.getSessionConfig.setIdleTime(IdleStatus.BOTH_IDLE, 10)

    val port = ServerStarter.port // XXX: use a better way to pass the parameters
    logger.info("Begin listening on port {}", port)
    acceptor.bind(new InetSocketAddress(port))
  }

  def process(message: Any) {
    ioHandler.send(message)
  }
}
