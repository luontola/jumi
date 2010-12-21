package net.orfjackal.dimdwarf.net

import org.junit.runner.RunWith
import org.hamcrest.Matchers._
import org.hamcrest.MatcherAssert.assertThat
import net.orfjackal.specsy._
import net.orfjackal.dimdwarf.mq.MessageQueue
import net.orfjackal.dimdwarf.util._
import org.apache.mina.core.buffer.IoBuffer
import net.orfjackal.dimdwarf.util.CustomMatchers._
import org.apache.mina.transport.socket.nio.NioSocketConnector
import org.apache.mina.core.service.IoHandlerAdapter
import org.apache.mina.core.session.IoSession
import java.net._
import net.orfjackal.dimdwarf.net.sgs._
import SimpleSgsProtocolReferenceMessages._

@RunWith(classOf[Specsy])
class NetworkActorSpec extends Spec {
  val TIMEOUT = 100L
  val port = SocketUtil.anyFreePort
  val toHub = new MessageQueue[Any]("toHub")

  val networkActor = new NetworkActor(port, toHub)
  networkActor.start()
  defer {networkActor.stop()}

  val client1 = new ClientRunner(port)
  defer {client1.disconnect()}

  val client2 = new ClientRunner(port)
  defer {client2.disconnect()}

  "Receives messages from clients and forwards them to the hub" >> {
    client1.sends(logoutRequest())

    assertHubReceives(LogoutRequest())
  }

  "Sends messages to clients" >> {
    val session1 = loginAndGetSessionHandleOf(client1)

    networkActor.process(SendToClient(LogoutSuccess(), session1))

    client1.assertReceived(logoutSuccess())
  }

  "Each client receives only the messages which are addressed to itself" >> {
    val session1 = loginAndGetSessionHandleOf(client1)
    val session2 = loginAndGetSessionHandleOf(client2)

    networkActor.process(SendToClient(LoginSuccess(), session1))
    client1.assertReceived(loginSuccess(Array()))

    networkActor.process(SendToClient(LoginFailure(), session2))
    client2.assertReceived(loginFailure(""))
  }


  private def loginAndGetSessionHandleOf(client: ClientRunner): SessionHandle = {
    client.sends(loginRequest("username", "password"))
    sessionHandleOfNextConnectedClient()
  }

  private def sessionHandleOfNextConnectedClient(): SessionHandle = {
    toHub.poll(TIMEOUT) match {
      case ReceivedFromClient(_, session) => session
    }
  }

  private def assertHubReceives(expected: ClientMessage) {
    toHub.poll(TIMEOUT) match {
      case ReceivedFromClient(actual, _) =>
        assertThat("hub received from client", expected, is(actual))
    }
  }

  class ClientRunner(port: Int) {
    private val received = new ByteSink(TIMEOUT)
    private val session = connectToPort(port)

    private def connectToPort(port: Int): IoSession = {
      val connector = new NioSocketConnector()
      connector.setHandler(new IoHandlerAdapter {
        override def messageReceived(session: IoSession, message: AnyRef) {
          received.append(message.asInstanceOf[IoBuffer])
        }
      })
      val connecting = connector.connect(new InetSocketAddress("localhost", port))
      connecting.awaitUninterruptibly(TIMEOUT);
      connecting.getSession
    }

    def disconnect() {
      session.close(true)
    }

    def sends(message: IoBuffer) {
      session.write(message)
    }

    def assertReceived(expected: IoBuffer) {
      assertEventually(received, startsWithBytes(expected))
    }
  }
}
