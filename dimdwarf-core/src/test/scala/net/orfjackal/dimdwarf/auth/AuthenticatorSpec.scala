package net.orfjackal.dimdwarf.auth

import org.junit.runner.RunWith
import net.orfjackal.specsy._
import net.orfjackal.dimdwarf.mq._
import net.orfjackal.dimdwarf.actors._
import org.mockito.Mockito._
import org.hamcrest.Matchers._
import org.hamcrest.MatcherAssert.assertThat
import scala.collection.mutable.Buffer

@RunWith(classOf[Specsy])
class AuthenticatorSpec extends Spec {
  val queues = new DeterministicMessageQueues

  val credentialsChecker = mock(classOf[CredentialsChecker[Credentials]])
  val validCredentials = new PasswordCredentials("username", "correct-password")
  val invalidCredentials = new PasswordCredentials("username", "wrong-password")
  when(credentialsChecker.isValid(validCredentials)).thenReturn(true)
  when(credentialsChecker.isValid(invalidCredentials)).thenReturn(false)

  val toAuthenticator = new MessageQueue[AuthenticatorMessage]("toAuthenticator")
  val authActor = new AuthenticatorActor(queues.toHub, credentialsChecker)
  val authController = new AuthenticatorController(toAuthenticator)
  queues.addController(authController)
  queues.addActor(authActor, toAuthenticator)

  "Logging with valid credentials succeeds" >> {
    var response = "-"

    authController.isUserAuthenticated(validCredentials,
      onYes = {response = "yes"},
      onNo = {response = "no"})
    queues.processMessagesUntilIdle()

    verify(credentialsChecker).isValid(validCredentials)
    assertThat(response, is("yes"))
  }

  "Logging with invalid credentials fails" >> {
    var response = "-"

    authController.isUserAuthenticated(invalidCredentials,
      onYes = {response = "yes"},
      onNo = {response = "no"})
    queues.processMessagesUntilIdle()

    verify(credentialsChecker).isValid(invalidCredentials)
    assertThat(response, is("no"))
  }

  "When the same user authenticates multiple times concurrently, each authentication attempt gets a response, in FIFO order" >> {
    val responses = Buffer[String]()

    authController.isUserAuthenticated(validCredentials,
      onYes = {responses.append("yes-1")}, onNo = {})
    authController.isUserAuthenticated(validCredentials,
      onYes = {responses.append("yes-2")}, onNo = {})
    authController.isUserAuthenticated(invalidCredentials,
      onYes = {}, onNo = {responses.append("no-3")})
    authController.isUserAuthenticated(invalidCredentials,
      onYes = {}, onNo = {responses.append("no-4")})
    queues.processMessagesUntilIdle()

    assertThat(responses, is(Buffer("yes-1", "yes-2", "no-3", "no-4")))
  }
}
