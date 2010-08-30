package net.orfjackal.dimdwarf.net

import org.junit.runner.RunWith
import org.junit.Assert._
import org.hamcrest.CoreMatchers._
import net.orfjackal.specsy._
import net.orfjackal.dimdwarf.util.SocketUtil
import net.orfjackal.dimdwarf.mq.MessageQueue
import net.orfjackal.dimdwarf.auth.Authenticator
import java.net.Socket
import SimpleSgsProtocolReferenceMessages._
import net.orfjackal.dimdwarf.services.DeterministicMessageQueues

@RunWith(classOf[Specsy])
class NetworkServiceSpec extends Spec {
  val queues = new DeterministicMessageQueues
  val port = SocketUtil.anyFreePort
  val authenticator = new SpyAuthenticator

  val network = new NetworkService(port, new SimpleSgsProtocolIoHandler(queues.toHub))
  val toNetwork = new MessageQueue[Any]("toNetwork")
  queues.addService(network, toNetwork)
  val networkCtrl = new NetworkController(toNetwork, authenticator)
  queues.addController(networkCtrl)

  network.start()
  defer {network.stop()}

  "Sends an authentication request when a client sends a login request" >> {
    val client = new Socket("localhost", port)
    defer {client.close()}
    val out = client.getOutputStream
    val in = client.getInputStream

    out.write(loginRequest("username", "password").array)
    queues.waitForMessages()
    queues.processMessagesUntilIdle()

    assertThat(queues.seenIn(queues.toHub).head, is(LoginRequest(): Any))
    assertThat(authenticator.lastMethod, is("isUserAuthenticated"))

    "Responds with a login failure when authentication fails" >> {
      authenticator.lastOnNo.apply()
      queues.processMessagesUntilIdle()

      assertThat(nextMessage(in), is(loginFailure("")))
    }
    // TODO: Responds with a login success when authentication succeeds
  }

  class SpyAuthenticator extends Authenticator {
    var lastMethod: String = null
    var lastOnNo: (() => Unit) = null
    var lastOnYes: (() => Unit) = null

    def isUserAuthenticated(onNo: => Unit) {
      lastMethod = "isUserAuthenticated"
      lastOnNo = onNo _
    }
  }
}
