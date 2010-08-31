package net.orfjackal.dimdwarf.net

import net.orfjackal.dimdwarf.mq.MessageSender
import com.google.inject.Inject
import net.orfjackal.dimdwarf.controller._
import net.orfjackal.dimdwarf.auth._

@ControllerScoped
class NetworkController @Inject()(toNetwork: MessageSender[Any], authenticator: Authenticator) extends Controller {
  def process(message: Any) {
    message match {
      case LoginRequest(username, password) =>
        authenticator.isUserAuthenticated(new PasswordCredentials(username, password),
          onYes = {toNetwork.send(LoginSuccess())},
          onNo = {toNetwork.send(LoginFailure())})
      case _ =>
    }
  }
}
