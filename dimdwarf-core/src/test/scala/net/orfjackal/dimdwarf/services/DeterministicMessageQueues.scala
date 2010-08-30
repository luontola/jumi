package net.orfjackal.dimdwarf.services

import net.orfjackal.dimdwarf.controller._
import net.orfjackal.dimdwarf.mq._

class DeterministicMessageQueues {
  private var services = Map[Service, MessageQueue[Any]]()
  private val hub = new ControllerHub
  val toHub = new MessageQueue[Any]("toHub")
  addService(hub, toHub)

  def addService(service: Service, toService: MessageQueue[Any]) {
    services = services.updated(service, toService)
  }

  def addController(controller: Controller) {
    hub.addController(controller)
  }

  def processMessagesUntilIdle() {
    var hadWork = false
    do {
      hadWork = false
      for ((service, queue) <- services) {
        if (processIfNonEmpty(service, queue)) {
          hadWork = true
        }
      }
    } while (hadWork)
  }

  private def processIfNonEmpty(service: Service, queue: MessageQueue[Any]): Boolean = {
    val message = queue.poll()
    val hasMessages = message != null
    if (hasMessages) {
      service.process(message)
    }
    hasMessages
  }
}
