package net.orfjackal.dimdwarf.net

import net.orfjackal.dimdwarf.mq.MessageSender
import com.google.inject.Inject
import net.orfjackal.dimdwarf.controller._
import net.orfjackal.dimdwarf.auth._
import net.orfjackal.dimdwarf.net.sgs._

@ControllerScoped
class NetworkController @Inject()(toNetwork: MessageSender[Any], authenticator: Authenticator) extends Controller {
  def process(message: Any) {
    message match {
      case ReceivedFromClient(message, session) =>
        processClientMessage(message, session)
      case _ =>
    }
  }

  private def processClientMessage(message: ClientMessage, session: SessionHandle) {
    message match {
      case LoginRequest(username, password) =>
        authenticator.isUserAuthenticated(new PasswordCredentials(username, password),
          onYes = {toNetwork.send(SendToClient(LoginSuccess(), session))},
          onNo = {toNetwork.send(SendToClient(LoginFailure(), session))})

      case LogoutRequest() =>
        toNetwork.send(SendToClient(LogoutSuccess(), session))

      case _ =>
        // TODO: do something smart, maybe disconnect the client if it sends a not allowed message
        assert(false, "Unsupported message: " + message)
    }
  }
}
