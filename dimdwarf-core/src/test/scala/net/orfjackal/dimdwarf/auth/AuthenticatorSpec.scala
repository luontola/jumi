package net.orfjackal.dimdwarf.auth

import org.junit.runner.RunWith
import net.orfjackal.specsy._
import net.orfjackal.dimdwarf.net._
import org.specs.SpecsMatchers
import net.orfjackal.dimdwarf.mq._
import net.orfjackal.dimdwarf.services._

@RunWith(classOf[Specsy])
class AuthenticatorSpec extends Spec with SpecsMatchers {
  val queues = new DeterministicMessageQueues

  val toAuthenticator = new MessageQueue[Any]("toAuthenticator")
  val authenticator = new AuthenticatorService(queues.toHub)
  val authenticatorCtrl = new AuthenticatorController(toAuthenticator)
  queues.addController(authenticatorCtrl)
  queues.addService(authenticator, toAuthenticator)

  val toNetwork = new MessageQueue[Any]("toNetwork")
  val networkCtrl = new NetworkController(toNetwork, authenticatorCtrl)
  queues.addController(networkCtrl)

  // TODO: decouple the authenticator and this test from the network service and controller

  "Logging with the wrong password fails" >> {
    queues.toHub.send(LoginRequest())

    queues.processMessagesUntilIdle()

    val response = toNetwork.poll()
    response must_== LoginFailure()
  }
}
