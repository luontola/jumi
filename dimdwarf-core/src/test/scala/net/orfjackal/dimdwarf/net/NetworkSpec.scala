package net.orfjackal.dimdwarf.net

import org.junit.runner.RunWith
import org.junit.Assert._
import org.hamcrest.CoreMatchers._
import net.orfjackal.specsy._
import net.orfjackal.dimdwarf.util.SocketUtil
import net.orfjackal.dimdwarf.mq.MessageQueue
import java.net.Socket
import SimpleSgsProtocolReferenceMessages._
import net.orfjackal.dimdwarf.actors.DeterministicMessageQueues
import net.orfjackal.dimdwarf.auth._

@RunWith(classOf[Specsy])
class NetworkSpec extends Spec {
  val queues = new DeterministicMessageQueues
  val port = SocketUtil.anyFreePort
  val authenticator = new SpyAuthenticator

  val networkActor = new NetworkActor(port, new SimpleSgsProtocolIoHandler(queues.toHub))
  val toNetwork = new MessageQueue[Any]("toNetwork")
  queues.addActor(networkActor, toNetwork)
  val networkCtrl = new NetworkController(toNetwork, authenticator)
  queues.addController(networkCtrl)

  networkActor.start()
  defer {networkActor.stop()}

  val client = new Socket("localhost", port)
  defer {client.close()}
  val out = client.getOutputStream
  val in = client.getInputStream

  val USERNAME = "John Doe"
  val PASSWORD = "secret"

  "When Client sends a login request" >> {
    out.write(loginRequest(USERNAME, PASSWORD).array)
    queues.waitForMessages()
    queues.processMessagesUntilIdle()

    "Actor sends the login request to Controller" >> {
      assertThat(queues.seenIn(queues.toHub).head, is(LoginRequest(USERNAME, PASSWORD): Any))
    }

    "Controller authenticates the username and password with Authenticator" >> {
      assertThat(authenticator.lastMethod, is("isUserAuthenticated"))
      assertThat(authenticator.lastCredentials, is(new PasswordCredentials(USERNAME, PASSWORD): Credentials))
    }

    "When authentication succeeds" >> {
      authenticator.lastOnYes.apply()
      queues.processMessagesUntilIdle()

      "Controller sends a success message to Actor" >> {
        assertThat(queues.seenIn(toNetwork).head, is(LoginSuccess(): Any))
      }
      "Actor sends the success message to Client" >> {
        val reconnectionKey = new Array[Byte](0) // TODO: create a reconnectionKey
        assertThat(nextMessage(in), is(loginSuccess(reconnectionKey)))
      }
    }

    "When authentication fails" >> {
      authenticator.lastOnNo.apply()
      queues.processMessagesUntilIdle()

      "Controller sends a failure message to Actor" >> {
        assertThat(queues.seenIn(toNetwork).head, is(LoginFailure(): Any))
      }
      "Actor sends the failure message to Client" >> {
        val reason = ""
        assertThat(nextMessage(in), is(loginFailure(reason)))
      }
    }
  }

  "When Client sends a logout request" >> {
    //    queues.toHub.send(LoginRequest(USERNAME, PASSWORD))
    //    queues.processMessagesUntilIdle()
    //    assertThat(networkCtrl.loggedInClients)
    // TODO: login the client

    out.write(logoutRequest().array)
    queues.waitForMessages()
    queues.processMessagesUntilIdle()

    "Actor sends the logout request to Controller" >> {
      assertThat(queues.seenIn(queues.toHub).head, is(LogoutRequest(): Any))
    }

    "Controller logs out the Client" // TODO: keep track of which clients are connected (implement with support for multiple clients)

    "Controller sends a logout message to Actor" >> {
      assertThat(queues.seenIn(toNetwork).head, is(LogoutSuccess(): Any))
    }
    "Actor sends the logout message to Client" >> {
    }
  }

  // TODO: when a client is not logged in, do not allow a logout request (or any other messages)

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
