package net.orfjackal.dimdwarf.controller

import net.orfjackal.dimdwarf.services._
import com.google.inject.Singleton

@Singleton
class ControllerHub extends Service {
  private var services = List[NonBlockingService]()

  def process(message: Any) {
    for (service <- services) {
      service.process(message)
    }
  }

  def addService(service: NonBlockingService) {
    services = service :: services
  }
}
