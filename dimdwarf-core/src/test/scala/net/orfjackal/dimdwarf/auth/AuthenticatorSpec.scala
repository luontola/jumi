package net.orfjackal.dimdwarf.auth

import org.junit.runner.RunWith
import net.orfjackal.specsy._
import net.orfjackal.dimdwarf.controller._
import net.orfjackal.dimdwarf.net._
import org.specs.SpecsMatchers
import net.orfjackal.dimdwarf.services.Service
import net.orfjackal.dimdwarf.mq._

@RunWith(classOf[Specsy])
class AuthenticatorSpec extends Spec with SpecsMatchers {
  val toHub = new MessageQueue[Any]
  val hub = new ControllerHub

  val toAuthenticator = new MessageQueue[Any]
  val authenticator = new Authenticator(toHub)
  val authenticatorCtrl = new AuthenticatorController(toAuthenticator)
  hub.addController(authenticatorCtrl)

  val toNetwork = new MessageQueue[Any]
  val networkCtrl = new NetworkController(toNetwork, authenticatorCtrl)
  hub.addController(networkCtrl)

  // TODO: decouple the authenticator and this test from the network service and controller

  "Logging with the wrong password fails" >> {
    toHub.send(LoginRequest())

    processMessagesUntilEmpty(Map(
      toHub -> hub,
      toAuthenticator -> authenticator))

    val response = toNetwork.poll()
    response must_== LoginFailure()
  }


  def processMessagesUntilEmpty(queues: Map[MessageQueue[Any], Service]) {
    var hadWork = false
    do {
      hadWork = false
      for ((queue, service) <- queues) {
        if (processIfNonEmpty(queue, service)) {
          hadWork = true
        }
      }
    } while (hadWork)
  }

  private def processIfNonEmpty(queue: MessageQueue[Any], service: Service): Boolean = {
    val message = queue.poll()
    val hasMessages = message != null
    if (hasMessages) {
      service.process(message)
    }
    hasMessages
  }
}
