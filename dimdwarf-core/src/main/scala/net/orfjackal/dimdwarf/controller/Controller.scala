package net.orfjackal.dimdwarf.controller

import net.orfjackal.dimdwarf.mq._
import net.orfjackal.dimdwarf.net._

class Controller {
  private var toNetwork: MessageSender[Any] = null

  def process(message: Any) {
    message match {
      case RegisterNetworkService(toService) =>
        toNetwork = toService // TODO: figure out a better way to register network services; perhaps one MQ per client?
      case LoginRequest() =>
        toNetwork.send(LoginFailure()) // TODO: move to authenticator
    }
  }
}
