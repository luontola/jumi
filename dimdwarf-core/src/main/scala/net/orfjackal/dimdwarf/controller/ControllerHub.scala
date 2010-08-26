package net.orfjackal.dimdwarf.controller

import net.orfjackal.dimdwarf.services._
import com.google.inject.Singleton

@Singleton
class ControllerHub extends Service {
  private var controllers = List[Controller]()

  def start() {
  }

  def process(message: Any) {
    for (controller <- controllers) {
      controller.process(message)
    }
  }

  def addController(service: Controller) {
    controllers = service :: controllers
  }
}
