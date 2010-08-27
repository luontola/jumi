package net.orfjackal.dimdwarf.controller

import net.orfjackal.dimdwarf.services._
import scala.collection.mutable.ArrayBuffer

@ControllerScoped
class ControllerHub extends Service {
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
