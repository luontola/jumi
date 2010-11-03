package net.orfjackal.dimdwarf.auth

import net.orfjackal.dimdwarf.mq.MessageSender
import com.google.inject.Inject
import net.orfjackal.dimdwarf.controller._

@ControllerScoped
class AuthenticatorController @Inject()(toAuthenticator: MessageSender[Any]) extends Controller with Authenticator {
  // TODO: write a test for concurrent authentications from the same user
  // TODO: remove old callbacks
  private var pending = Map[Credentials, Callbacks]()

  def isUserAuthenticated(credentials: Credentials, onYes: => Unit, onNo: => Unit) {
    toAuthenticator.send(IsUserAuthenticated(credentials))
    pending = pending.updated(credentials, new Callbacks(onYes _, onNo _))
  }

  def process(message: Any) {
    message match {
      case YesUserIsAuthenticated(credentials) =>
        pending(credentials).fireSuccess()
      case NoUserIsNotAuthenticated(credentials) =>
        pending(credentials).fireFailure()
      case _ =>
    }
  }

  private class Callbacks(onYes: Function0[Unit], onNo: Function0[Unit]) {
    def fireSuccess() = onYes()

    def fireFailure() = onNo()
  }
}
