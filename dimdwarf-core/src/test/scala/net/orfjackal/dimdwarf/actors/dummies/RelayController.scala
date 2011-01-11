package net.orfjackal.dimdwarf.actors.dummies

import net.orfjackal.dimdwarf.mq.MessageSender
import net.orfjackal.dimdwarf.controller._
import javax.inject.Inject

@ControllerScoped
class RelayController @Inject()(toActor: MessageSender[Any], spy: Spy) extends Controller {
  def process(message: Any) {
    message match {
      case MessageToActor(m) => {
        spy.log("controller forwarded " + m)
        toActor.send(m)
      }
      case m => spy.log("controller got " + m)
    }
  }
}
