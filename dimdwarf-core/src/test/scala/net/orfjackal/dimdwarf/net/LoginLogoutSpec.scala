package net.orfjackal.dimdwarf.net

import org.junit.runner.RunWith
import org.hamcrest.Matchers._
import org.hamcrest.MatcherAssert.assertThat
import net.orfjackal.specsy._
import net.orfjackal.dimdwarf.mq.MessageQueue
import net.orfjackal.dimdwarf.auth._
import net.orfjackal.dimdwarf.actors._
import net.orfjackal.dimdwarf.net.sgs._

@RunWith(classOf[Specsy])
class LoginLogoutSpec extends Spec {
  val queues = new DeterministicMessageQueues
  val authenticator = new SpyAuthenticator
  val networkActor = new DummyNetworkActor()

  val toNetwork = new MessageQueue[NetworkMessage]("toNetwork")
  queues.addActor(networkActor, toNetwork)
  val networkCtrl = new NetworkController(toNetwork, authenticator)
  queues.addController(networkCtrl)

  val USERNAME = "John Doe"
  val PASSWORD = "secret"
  val SESSION = DummySessionHandle()

  "When a client sends a login request" >> {
    clientSends(LoginRequest(USERNAME, PASSWORD))

    "NetworkController authenticates the username and password with Authenticator" >> {
      assertThat(authenticator.lastMethod, is("isUserAuthenticated"))
      assertThat(authenticator.lastCredentials, is(new PasswordCredentials(USERNAME, PASSWORD): Credentials))
    }

    "If authentication succeeds" >> {
      authenticator.lastOnYes.apply()
      queues.processMessagesUntilIdle()

      "NetworkController sends a success message to the client" >> {
        assertMessageSent(toNetwork, SendToClient(LoginSuccess(), SESSION))
      }
    }

    "If authentication fails" >> {
      authenticator.lastOnNo.apply()
      queues.processMessagesUntilIdle()

      "NetworkController sends a failure message to the client" >> {
        assertMessageSent(toNetwork, SendToClient(LoginFailure(), SESSION))
      }
    }
  }

  "When a client sends a logout request" >> {
    //    queues.toHub.send(LoginRequest(USERNAME, PASSWORD))
    //    queues.processMessagesUntilIdle()
    //    assertThat(networkCtrl.loggedInClients)
    // TODO: login the client

    clientSends(LogoutRequest())

    // TODO: keep track of which clients are connected (cam be test-driven with JMX monitoring or session messages)
    "and NetworkController logs out the client"

    "after which NetworkController sends a logout success message to the client" >> {
      assertMessageSent(toNetwork, SendToClient(LogoutSuccess(), SESSION))
    }
  }

  // TODO: when a client is not logged in, do not allow a logout request (or any other messages)

  private def assertMessageSent(queue: MessageQueue[NetworkMessage], expected: Any) {
    assertThat(queues.seenIn(queue).head, is(expected))
  }

  private def clientSends(message: ClientMessage) {
    queues.toHub.send(ReceivedFromClient(message, SESSION))
    queues.processMessagesUntilIdle()
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

  class DummyNetworkActor extends Actor[NetworkMessage] {
    def start() {}

    def process(message: NetworkMessage) {}
  }

  case class DummySessionHandle() extends SessionHandle
}
