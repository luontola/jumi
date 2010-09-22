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
import org.apache.mina.core.buffer.IoBuffer

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
  val toController = queues.toHub

  networkActor.start()
  defer {networkActor.stop()}

  val client = new Socket("localhost", port)
  defer {client.close()}
  val clientToServer = client.getOutputStream
  val clientFromServer = client.getInputStream

  val USERNAME = "John Doe"
  val PASSWORD = "secret"

  "When Client sends a login request" >> {
    clientSends(loginRequest(USERNAME, PASSWORD))

    "Actor sends the login request to Controller" >> {
      assertMessageSent(toController, LoginRequest(USERNAME, PASSWORD))
    }

    "Controller authenticates the username and password with Authenticator" >> {
      assertThat(authenticator.lastMethod, is("isUserAuthenticated"))
      assertThat(authenticator.lastCredentials, is(new PasswordCredentials(USERNAME, PASSWORD): Credentials))
    }

    "When authentication succeeds" >> {
      authenticator.lastOnYes.apply()
      queues.processMessagesUntilIdle()

      "Controller sends a success message to Actor" >> {
        assertMessageSent(toNetwork, LoginSuccess())
      }
      "Actor sends the success message to Client" >> {
        val reconnectionKey = new Array[Byte](0) // TODO: create a reconnectionKey
        assertClientReceived(loginSuccess(reconnectionKey))
      }
    }

    "When authentication fails" >> {
      authenticator.lastOnNo.apply()
      queues.processMessagesUntilIdle()

      "Controller sends a failure message to Actor" >> {
        assertMessageSent(toNetwork, LoginFailure())
      }
      "Actor sends the failure message to Client" >> {
        assertClientReceived(loginFailure(reason = ""))
      }
    }
  }

  "When Client sends a logout request" >> {
    //    queues.toHub.send(LoginRequest(USERNAME, PASSWORD))
    //    queues.processMessagesUntilIdle()
    //    assertThat(networkCtrl.loggedInClients)
    // TODO: login the client

    clientSends(logoutRequest())

    "Actor sends the logout request to Controller" >> {
      assertMessageSent(toController, LogoutRequest())
    }

    "Controller logs out the Client" // TODO: keep track of which clients are connected (implement with support for multiple clients)

    "Controller sends a logout message to Actor" >> {
      assertMessageSent(toNetwork, LogoutSuccess())
    }
    "Actor sends the logout message to Client" >> {
      assertClientReceived(logoutSuccess())
    }
  }

  // TODO: when a client is not logged in, do not allow a logout request (or any other messages)

  private def assertMessageSent(queue: MessageQueue[Any], expected: Any) {
    assertThat(queues.seenIn(queue).head, is(expected))
  }

  private def clientSends(message: IoBuffer): Unit = {
    clientToServer.write(message.array)
    queues.waitForMessages()
    queues.processMessagesUntilIdle()
  }

  private def assertClientReceived(expected: IoBuffer): Unit = {
    assertThat(nextMessageToClient(), is(expected: Any))
  }

  private def nextMessageToClient(): IoBuffer = {
    // TODO: write a utility class for asynchronous tests, which timeout when there is no event
    nextMessage(clientFromServer)
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
