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

  "When a client sends a login request" >> {
    clientSends(loginRequest(USERNAME, PASSWORD))

    "NetworkActor sends the login request to NetworkController" >> {
      assertMessageSent(toController, LoginRequest(USERNAME, PASSWORD))
    }
    "and NetworkController authenticates the username and password with Authenticator" >> {
      assertThat(authenticator.lastMethod, is("isUserAuthenticated"))
      assertThat(authenticator.lastCredentials, is(new PasswordCredentials(USERNAME, PASSWORD): Credentials))
    }

    "If authentication succeeds" >> {
      authenticator.lastOnYes.apply()
      queues.processMessagesUntilIdle()

      "NetworkController sends a success message to NetworkActor" >> {
        assertMessageSent(toNetwork, LoginSuccess())
      }
      "after which NetworkActor forwards the success message to the client" >> {
        val reconnectionKey = new Array[Byte](0) // TODO: create a reconnectionKey
        assertClientReceived(loginSuccess(reconnectionKey))
      }
    }

    "If authentication fails" >> {
      authenticator.lastOnNo.apply()
      queues.processMessagesUntilIdle()

      "NetworkController sends a failure message to NetworkActor" >> {
        assertMessageSent(toNetwork, LoginFailure())
      }
      "after which NetworkActor forwards the failure message to the client" >> {
        assertClientReceived(loginFailure(reason = ""))
      }
    }
  }

  "When a client sends a logout request" >> {
    //    queues.toHub.send(LoginRequest(USERNAME, PASSWORD))
    //    queues.processMessagesUntilIdle()
    //    assertThat(networkCtrl.loggedInClients)
    // TODO: login the client

    clientSends(logoutRequest())

    "NetworkActor sends the logout request to NetworkController" >> {
      assertMessageSent(toController, LogoutRequest())
    }
    "and NetworkController logs out the client" // TODO: keep track of which clients are connected (implement with support for multiple clients)

    "after which NetworkController sends a logout success message to NetworkActor" >> {
      assertMessageSent(toNetwork, LogoutSuccess())
    }
    "after which NetworkActor forwards the logout success message to the client" >> {
      assertClientReceived(logoutSuccess())
    }
  }

  // TODO: when a client is not logged in, do not allow a logout request (or any other messages)

  private def assertMessageSent(queue: MessageQueue[Any], expected: Any) {
    assertThat(queues.seenIn(queue).head, is(expected))
  }

  private def clientSends(message: IoBuffer) {
    clientToServer.write(message.array)
    queues.waitForMessages()
    queues.processMessagesUntilIdle()
  }

  private def assertClientReceived(expected: IoBuffer) {
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
