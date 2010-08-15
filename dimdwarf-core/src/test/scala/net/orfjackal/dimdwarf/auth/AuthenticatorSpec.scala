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
  val controller = new Controller
  val toController = new MessageQueue[Any]

  val authenticator = new Authenticator(toController)
  val toAuthenticator = new MessageQueue[Any]

  val toNetwork = new MessageQueue[Any]

  toController.send(RegisterAuthenticatorService(toAuthenticator))
  toController.send(RegisterNetworkService(toNetwork))

  // TODO: decouple the authenticator and this test from the network service and controller

  "Logging with the wrong password fails" >> {
    toController.send(LoginRequest())

    processMessagesUntilEmpty(Map(
      toController -> controller,
      toAuthenticator -> authenticator))

    val response = toNetwork.poll()
    response must_== LoginFailure()
  }


  def processMessagesUntilEmpty(queues: Map[MessageReceiver[Any], Service]) {
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

  private def processIfNonEmpty(queue: MessageReceiver[Any], service: Service): Boolean = {
    val message = queue.poll()
    val hasMessages = message != null
    if (hasMessages) {
      service.process(message)
    }
    hasMessages
  }
}
