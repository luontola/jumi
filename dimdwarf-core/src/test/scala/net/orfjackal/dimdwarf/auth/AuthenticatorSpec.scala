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
  val toController = new MessageQueue[Any]
  val controller = new ControllerHub

  val toAuthenticator = new MessageQueue[Any]
  val authenticator = new Authenticator(toController)
  val authenticatorCtrl = new AuthenticatorController(toAuthenticator)
  controller.addService(authenticatorCtrl)

  val toNetwork = new MessageQueue[Any]
  val networkCtrl = new NetworkController(toNetwork, authenticatorCtrl)
  controller.addService(networkCtrl)

  // TODO: decouple the authenticator and this test from the network service and controller

  "Logging with the wrong password fails" >> {
    toController.send(LoginRequest())

    processMessagesUntilEmpty(Map(
      toController -> controller,
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
