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

  val toNetwork = new MessageQueue[Any]("toNetwork")
  queues.addActor(networkActor, toNetwork)
  val networkCtrl = new NetworkController(toNetwork, authenticator)
  queues.addController(networkCtrl)

  val USERNAME = "John Doe"
  val PASSWORD = "secret"

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
        assertMessageSent(toNetwork, SendToClient(LoginSuccess()))
      }
    }

    "If authentication fails" >> {
      authenticator.lastOnNo.apply()
      queues.processMessagesUntilIdle()

      "NetworkController sends a failure message to the client" >> {
        assertMessageSent(toNetwork, SendToClient(LoginFailure()))
      }
    }
  }

  "When a client sends a logout request" >> {
    //    queues.toHub.send(LoginRequest(USERNAME, PASSWORD))
    //    queues.processMessagesUntilIdle()
    //    assertThat(networkCtrl.loggedInClients)
    // TODO: login the client

    clientSends(LogoutRequest())

    "and NetworkController logs out the client" // TODO: keep track of which clients are connected (implement with support for multiple clients)

    "after which NetworkController sends a logout success message to the client" >> {
      assertMessageSent(toNetwork, SendToClient(LogoutSuccess()))
    }
  }

  // TODO: when a client is not logged in, do not allow a logout request (or any other messages)

  private def assertMessageSent(queue: MessageQueue[Any], expected: Any) {
    assertThat(queues.seenIn(queue).head, is(expected))
  }

  private def clientSends(message: ClientMessage) {
    queues.toHub.send(ReceivedFromClient(message, DummySessionHandle()))
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

  class DummyNetworkActor extends Actor {
    def start() {}

    def process(message: Any) {}
  }

  case class DummySessionHandle() extends SessionHandle
}
