package net.orfjackal.dimdwarf.auth

import net.orfjackal.dimdwarf.mq.MessageSender
import com.google.inject.Inject
import net.orfjackal.dimdwarf.controller.Hub
import net.orfjackal.dimdwarf.actors._

@ActorScoped
class AuthenticatorActor @Inject()(@Hub toHub: MessageSender[Any], credentialsChecker: CredentialsChecker[Credentials]) extends Actor {
  def start() {
  }

  def process(message: Any) {
    message match {
      case IsUserAuthenticated(credentials) =>
        if (credentialsChecker.isValid(credentials)) {
          toHub.send(YesUserIsAuthenticated())
        } else {
          toHub.send(NoUserIsNotAuthenticated())
        }
    }
  }
}
