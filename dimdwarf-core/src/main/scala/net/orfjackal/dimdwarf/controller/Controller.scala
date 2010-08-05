package net.orfjackal.dimdwarf.controller

import net.orfjackal.dimdwarf.mq._
import net.orfjackal.dimdwarf.net._
import org.slf4j.LoggerFactory
import Controller._

object Controller {
  private val logger = LoggerFactory.getLogger(classOf[Controller])
}

class Controller extends MessageSender[Any] {
  private val messagesToController = new MessageQueue[Any]
  private var toNetwork: MessageSender[Any] = null

  def send(message: Any) {
    messagesToController.send(message)
  }

  def processNextMessage() {
    val message = messagesToController.take()
    logger.debug("Processing message: {}", message)

    message match {
      case RegisterNetworkService(toService) =>
        toNetwork = toService // TODO: figure out a better way to register network services; perhaps one MQ per client?
      case LoginRequest() =>
        toNetwork.send(LoginFailure()) // TODO: move to authenticator
    }
  }
}
