package net.orfjackal.dimdwarf.controller

import net.orfjackal.dimdwarf.mq._
import net.orfjackal.dimdwarf.net._
import net.orfjackal.dimdwarf.auth._
import net.orfjackal.dimdwarf.services._
import com.google.inject.Singleton

@Singleton
class ControllerHub extends Service {
  private var toNetwork: MessageSender[Any] = null
  private var toAuthenticator: MessageSender[Any] = null

  // TODO: make registering services generic
  // TODO: make it possible for ServiceControllers to call each other directly; hide the service's MQ behind the service's controller

  private val serviceController: PartialFunction[Any, Unit] = {
    case RegisterNetworkService(toService) =>
      toNetwork = toService
    case RegisterAuthenticatorService(toService) =>
      toAuthenticator = toService
  }
  private val authenticatorController: PartialFunction[Any, Unit] = {
    case NoUserIsNotAuthenticated() =>
      toNetwork.send(LoginFailure()) // TODO: implement as a callback to decouple authenticator from network
    case YesUserIsAuthenticated() => // TODO
  }
  private val networkController: PartialFunction[Any, Unit] = {
    case LoginRequest() =>
      toAuthenticator.send(IsUserAuthenticated())
  }

  def process(message: Any) {
    if (serviceController.isDefinedAt(message)) {
      serviceController.apply(message)
    }
    if (authenticatorController.isDefinedAt(message)) {
      authenticatorController.apply(message)
    }
    if (networkController.isDefinedAt(message)) {
      networkController.apply(message)
    }
  }
}
