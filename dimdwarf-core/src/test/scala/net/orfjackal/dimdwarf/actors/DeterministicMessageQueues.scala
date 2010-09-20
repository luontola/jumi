package net.orfjackal.dimdwarf.actors

import net.orfjackal.dimdwarf.controller._
import net.orfjackal.dimdwarf.mq._
import scala.collection.mutable.Buffer

class DeterministicMessageQueues {
  private var actors = Map[MessageQueue[Any], Actor]()
  private var seenMessages = Map[MessageQueue[Any], Buffer[Any]]()

  private val hub = new ControllerHub
  val toHub = new MessageQueue[Any]("toHub")
  addActor(hub, toHub)

  def addActor(actor: Actor, toActor: MessageQueue[Any]) {
    actors = actors.updated(toActor, actor)
    seenMessages = seenMessages.updated(toActor, Buffer())
  }

  def addController(controller: Controller) {
    hub.addController(controller)
  }

  def seenIn(toActor: MessageQueue[Any]): Seq[Any] = {
    seenMessages(toActor).toSeq
  }

  def waitForMessages() {
    while (!hasMessages) {
      Thread.sleep(1)
    }
  }

  private def hasMessages: Boolean = actors.keys.count(!_.isEmpty) > 0

  def processMessagesUntilIdle() {
    var hadWork = false
    do {
      hadWork = false
      for ((toActor, actor) <- actors) {
        if (processIfNonEmpty(toActor, actor)) {
          hadWork = true
        }
      }
    } while (hadWork)
  }

  private def processIfNonEmpty(toActor: MessageQueue[Any], actor: Actor): Boolean = {
    val message = toActor.poll()
    val hasMessages = message != null
    if (hasMessages) {
      seenMessages(toActor).append(message)
      actor.process(message)
    }
    hasMessages
  }
}
