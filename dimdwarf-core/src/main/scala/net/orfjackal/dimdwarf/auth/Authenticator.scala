package net.orfjackal.dimdwarf.auth

import net.orfjackal.dimdwarf.mq.MessageSender
import net.orfjackal.dimdwarf.services.Service
import com.google.inject.Inject
import net.orfjackal.dimdwarf.controller.Controller

class Authenticator @Inject()(@Controller toController: MessageSender[Any]) extends Service {
  def process(message: Any) {
    message match {
      case IsUserAuthenticated() =>
        // (here may connect to DB or do similar blocking activity)
        toController.send(NoUserIsNotAuthenticated())
    }
  }
}
