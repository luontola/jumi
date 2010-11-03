package net.orfjackal.dimdwarf.auth

import net.orfjackal.dimdwarf.mq.MessageSender
import com.google.inject.Inject
import net.orfjackal.dimdwarf.controller._

@ControllerScoped
class AuthenticatorController @Inject()(toAuthenticator: MessageSender[Any]) extends Controller with Authenticator {
  private var yesCallbacks: Option[Function0[Unit]] = None
  private var noCallbacks: Option[Function0[Unit]] = None

  // TODO: support more than one client
  // TODO: remove old callbacks
  def isUserAuthenticated(credentials: Credentials, onYes: => Unit, onNo: => Unit) {
    toAuthenticator.send(IsUserAuthenticated(credentials))
    yesCallbacks = Some(onYes _)
    noCallbacks = Some(onNo _)
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
