package net.orfjackal.dimdwarf.auth

import net.orfjackal.dimdwarf.mq.MessageSender
import com.google.inject.Inject
import net.orfjackal.dimdwarf.controller._

@ControllerScoped
class AuthenticatorController @Inject()(toAuthenticator: MessageSender[AuthenticatorMessage]) extends Controller with Authenticator {
  // TODO: pass callbacks with the message (as a private class), to avoid local state and to simplify the message contracts
  private var pending = Map[Credentials, Seq[Callback]]().withDefaultValue(Seq())

  def isUserAuthenticated(credentials: Credentials, onYes: => Unit, onNo: => Unit) {
    toAuthenticator.send(IsUserAuthenticated(credentials))
    addCallback(credentials, onYes, onNo)
  }

  def process(message: Any) {
    message match {
      case YesUserIsAuthenticated(credentials) =>
        callAndResetCallbacks(credentials, _.fireSuccess())
      case NoUserIsNotAuthenticated(credentials) =>
        callAndResetCallbacks(credentials, _.fireFailure())
      case _ =>
    }
  }

  private def addCallback(credentials: Credentials, onYes: => Unit, onNo: => Unit): Unit = {
    val oldCallbacks = pending(credentials)
    val newCallbacks = oldCallbacks :+ new Callback(onYes _, onNo _)
    pending += (credentials -> newCallbacks)
  }

  private def callAndResetCallbacks(credentials: Credentials, call: (Callback) => Any) {
    pending(credentials).foreach(call)
    pending -= credentials
  }

  private class Callback(onYes: Function0[Unit], onNo: Function0[Unit]) {
    def fireSuccess() = onYes()

    def fireFailure() = onNo()
  }
}
