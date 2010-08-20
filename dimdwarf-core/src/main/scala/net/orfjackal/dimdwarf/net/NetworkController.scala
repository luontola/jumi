package net.orfjackal.dimdwarf.net

import net.orfjackal.dimdwarf.controller.Controller
import net.orfjackal.dimdwarf.auth.AuthenticatorController
import net.orfjackal.dimdwarf.mq.MessageSender

class NetworkController(toNetwork: MessageSender[Any], authenticator: AuthenticatorController) extends Controller {
  def process(message: Any) {
    message match {
      case LoginRequest() =>
        authenticator.isUserAuthenticated({toNetwork.send(LoginFailure())})
      case _ =>
    }
  }
}
