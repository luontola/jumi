package net.orfjackal.dimdwarf.auth

import net.orfjackal.dimdwarf.mq.MessageSender
import com.google.inject.Inject
import net.orfjackal.dimdwarf.controller.Hub
import net.orfjackal.dimdwarf.services._

@ServiceScoped
class AuthenticatorService @Inject()(@Hub toHub: MessageSender[Any]) extends Service {
  def start() {
  }

  def process(message: Any) {
    message match {
      case IsUserAuthenticated() =>
        // (here may connect to DB or do similar blocking activity)
        toHub.send(NoUserIsNotAuthenticated())
    }
  }
}
