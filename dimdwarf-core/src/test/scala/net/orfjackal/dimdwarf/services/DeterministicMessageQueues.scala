package net.orfjackal.dimdwarf.services

import net.orfjackal.dimdwarf.controller._
import net.orfjackal.dimdwarf.mq._
import scala.collection.mutable.Buffer

class DeterministicMessageQueues {
  private var services = Map[MessageQueue[Any], Service]()
  private var seenMessages = Map[MessageQueue[Any], Buffer[Any]]()

  private val hub = new ControllerHub
  val toHub = new MessageQueue[Any]("toHub")
  addService(hub, toHub)

  def addService(service: Service, toService: MessageQueue[Any]) {
    services = services.updated(toService, service)
    seenMessages = seenMessages.updated(toService, Buffer())
  }

  def addController(controller: Controller) {
    hub.addController(controller)
  }

  def seenIn(toService: MessageQueue[Any]): Seq[Any] = {
    seenMessages(toService).toSeq
  }

  def waitForMessages() {
    while (!hasMessages) {
      Thread.sleep(1)
    }
  }

  private def hasMessages: Boolean = services.keys.count(!_.isEmpty) > 0

  def processMessagesUntilIdle() {
    var hadWork = false
    do {
      hadWork = false
      for ((toService, service) <- services) {
        if (processIfNonEmpty(toService, service)) {
          hadWork = true
        }
      }
    } while (hadWork)
  }

  private def processIfNonEmpty(toService: MessageQueue[Any], service: Service): Boolean = {
    val message = toService.poll()
    val hasMessages = message != null
    if (hasMessages) {
      seenMessages(toService).append(message)
      service.process(message)
    }
    hasMessages
  }
}
