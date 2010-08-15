package net.orfjackal.dimdwarf.controller

import net.orfjackal.dimdwarf.mq._
import net.orfjackal.dimdwarf.net._
import net.orfjackal.dimdwarf.auth._
import net.orfjackal.dimdwarf.services.Service

class Controller extends Service {
  private var toNetwork: MessageSender[Any] = null
  private var toAuthenticator: MessageSender[Any] = null

  def process(message: Any) {
    message match {
      case RegisterNetworkService(toService) =>
        toNetwork = toService // TODO: figure out a better way to register network services; perhaps one MQ per client?
      case RegisterAuthenticatorService(toService) =>
        toAuthenticator = toService

      case LoginRequest() =>
        toAuthenticator.send(IsUserAuthenticated())
      case YesUserIsAuthenticated() =>
      // TODO
      case NoUserIsNotAuthenticated() =>
        toNetwork.send(LoginFailure())
    }
  }
}
