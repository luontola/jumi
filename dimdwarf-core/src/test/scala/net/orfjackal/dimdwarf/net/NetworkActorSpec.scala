package net.orfjackal.dimdwarf.net

import org.junit.runner.RunWith
import org.hamcrest.Matchers._
import org.hamcrest.MatcherAssert.assertThat
import net.orfjackal.specsy._
import net.orfjackal.dimdwarf.mq.MessageQueue
import net.orfjackal.dimdwarf.util._
import java.net.Socket
import java.io.InputStream
import org.apache.mina.core.buffer.IoBuffer
import SimpleSgsProtocolReferenceMessages._
import net.orfjackal.dimdwarf.util.CustomMatchers._

@RunWith(classOf[Specsy])
class NetworkActorSpec extends Spec {
  val TIMEOUT = 100L
  val port = SocketUtil.anyFreePort
  val toHub = new MessageQueue[Any]("toHub")

  val networkActor = new NetworkActor(port, new SimpleSgsProtocolIoHandler(toHub))
  networkActor.start()
  defer {networkActor.stop()}

  val client = new Socket("localhost", port)
  defer {client.close()}
  val clientToServer = client.getOutputStream
  val clientFromServer = new ByteSink(TIMEOUT)
  copyInBackground(client.getInputStream, clientFromServer)

  "Receives messages from clients and forwards them to the hub" >> {
    clientSends(logoutRequest())

    assertHubReceives(LogoutRequest())
  }

  "Sends messages to clients" >> {
    givenClientHasConnected()

    networkActor.process(LogoutSuccess())

    assertClientReceived(logoutSuccess())
  }


  private def givenClientHasConnected() {
    clientSends(loginRequest("username", "password"))
    assertHubReceives(LoginRequest("username", "password"))
  }

  private def clientSends(message: IoBuffer) {
    clientToServer.write(message.array)
  }

  private def assertClientReceived(expected: IoBuffer) {
    assertEventually(clientFromServer, startsWithBytes(expected))
  }

  private def assertHubReceives(expected: Any) {
    assertThat(toHub.poll(TIMEOUT), is(expected))
  }

  private def copyInBackground(source: InputStream, target: ByteSink) {
    // TODO: try using Apache MINA's client library to get event-driven IoBuffers for free
    val t = new Thread(new Runnable {
      def run() {
        val buf = new Array[Byte](100);
        var len = 0;
        try {
          do {
            // TODO: handle "java.net.SocketException: socket closed"
            len = source.read(buf)
            target.append(IoBuffer.wrap(buf, 0, len))
          } while (len >= 0)
        } finally {
          source.close()
        }
      }
    })
    t.setDaemon(true)
    t.start()
  }
}
