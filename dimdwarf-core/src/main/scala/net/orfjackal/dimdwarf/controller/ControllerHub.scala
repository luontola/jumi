package net.orfjackal.dimdwarf.controller

import scala.collection.mutable.ArrayBuffer
import net.orfjackal.dimdwarf.actors.Actor

@ControllerScoped
class ControllerHub extends Actor {
  private val controllers = new ArrayBuffer[Controller]

  def start() {
  }

  def process(message: Any) {
    controllers.foreach(_.process(message))
  }

  def addController(controller: Controller) {
    controllers.append(controller)
  }
}
