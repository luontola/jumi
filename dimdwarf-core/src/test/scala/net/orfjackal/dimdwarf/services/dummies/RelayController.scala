package net.orfjackal.dimdwarf.services.dummies

import net.orfjackal.dimdwarf.mq.MessageSender
import net.orfjackal.dimdwarf.controller._
import com.google.inject.Inject

@ControllerScoped
class RelayController @Inject()(toService: MessageSender[Any], spy: Spy) extends Controller {
  def process(message: Any) {
    message match {
      case MessageToService(m) => {
        spy.log("controller forwarded " + m)
        toService.send(m)
      }
      case m => spy.log("controller got " + m)
    }
  }
}
