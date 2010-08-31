package net.orfjackal.dimdwarf.net

import org.junit.runner.RunWith
import org.junit.Assert._
import org.hamcrest.CoreMatchers._
import net.orfjackal.specsy._
import net.orfjackal.dimdwarf.util.SocketUtil
import net.orfjackal.dimdwarf.mq.MessageQueue
import java.net.Socket
import SimpleSgsProtocolReferenceMessages._
import net.orfjackal.dimdwarf.services.DeterministicMessageQueues
import net.orfjackal.dimdwarf.auth._

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

  "When Client sends a login request" >> {
    val client = new Socket("localhost", port)
    defer {client.close()}
    val out = client.getOutputStream
    val in = client.getInputStream

    val USERNAME = "John Doe"
    val PASSWORD = "secret"
    out.write(loginRequest(USERNAME, PASSWORD).array)
    queues.waitForMessages()
    queues.processMessagesUntilIdle()

    "Service sends the login request to Controller" >> {
      assertThat(queues.seenIn(queues.toHub).head, is(LoginRequest(USERNAME, PASSWORD): Any))
    }

    "Controller authenticates the username and password with Authenticator" >> {
      assertThat(authenticator.lastMethod, is("isUserAuthenticated"))
      assertThat(authenticator.lastCredentials, is(new PasswordCredentials(USERNAME, PASSWORD): Credentials))
    }

    "When authentication succeeds" >> {
      authenticator.lastOnYes.apply()
      queues.processMessagesUntilIdle()

      "Controller sends a success message to Service" >> {
        assertThat(queues.seenIn(toNetwork).head, is(LoginSuccess(): Any))
      }
      "Service sends the success message to Client" >> {
        val reconnectionKey = new Array[Byte](0) // TODO: create a reconnectionKey
        assertThat(nextMessage(in), is(loginSuccess(reconnectionKey)))
      }
    }

    "When authentication fails" >> {
      authenticator.lastOnNo.apply()
      queues.processMessagesUntilIdle()

      "Controller sends a failure message to Service" >> {
        assertThat(queues.seenIn(toNetwork).head, is(LoginFailure(): Any))
      }
      "Service sends the failure message to Client" >> {
        val reason = ""
        assertThat(nextMessage(in), is(loginFailure(reason)))
      }
    }
  }

  class SpyAuthenticator extends Authenticator {
    var lastMethod: String = null
    var lastCredentials: Credentials = null
    var lastOnNo: (() => Unit) = null
    var lastOnYes: (() => Unit) = null

    def isUserAuthenticated(credentials: Credentials, onYes: => Unit, onNo: => Unit) {
      lastMethod = "isUserAuthenticated"
      lastCredentials = credentials
      lastOnYes = onYes _
      lastOnNo = onNo _
    }
  }
}
