package net.orfjackal.dimdwarf.auth

import net.orfjackal.dimdwarf.mq.MessageSender
import com.google.inject.Inject
import net.orfjackal.dimdwarf.controller._

@ControllerScoped
class AuthenticatorController @Inject()(toAuthenticator: MessageSender[Any]) extends Controller with Authenticator {
  private var yesCallbacks = List[Function0[Unit]]()
  private var noCallbacks = List[Function0[Unit]]()

  // TODO: support more than one client
  // TODO: remove old callbacks
  def isUserAuthenticated(credentials: Credentials, onYes: => Unit, onNo: => Unit) {
    toAuthenticator.send(IsUserAuthenticated(credentials))
    yesCallbacks = onYes _ :: yesCallbacks
    noCallbacks = onNo _ :: noCallbacks
  }

  def process(message: Any) {
    message match {
      case YesUserIsAuthenticated() =>
        yesCallbacks.foreach(_.apply())
      case NoUserIsNotAuthenticated() =>
        noCallbacks.foreach(_.apply())
      case _ =>
    }
  }
}
