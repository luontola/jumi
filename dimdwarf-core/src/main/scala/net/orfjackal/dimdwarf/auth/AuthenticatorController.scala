package net.orfjackal.dimdwarf.auth

import net.orfjackal.dimdwarf.mq.MessageSender
import com.google.inject.Inject
import net.orfjackal.dimdwarf.controller._

@ControllerScoped
class AuthenticatorController @Inject()(toAuthenticator: MessageSender[Any]) extends Controller {
  private var yesCallbacks = List[Function0[Unit]]()
  private var noCallbacks = List[Function0[Unit]]()

  def isUserAuthenticated(onNo: => Unit) {
    toAuthenticator.send(IsUserAuthenticated())
    // TODO: on yes
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
