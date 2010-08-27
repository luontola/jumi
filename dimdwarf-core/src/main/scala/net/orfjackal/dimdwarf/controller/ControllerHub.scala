package net.orfjackal.dimdwarf.controller

import net.orfjackal.dimdwarf.services._

@ControllerScoped
class ControllerHub extends Service {
  private var controllers = List[Controller]()

  def start() {
  }

  def process(message: Any) {
    for (controller <- controllers) {
      controller.process(message)
    }
  }

  def addController(controller: Controller) {
    controllers = controller :: controllers
  }
}
