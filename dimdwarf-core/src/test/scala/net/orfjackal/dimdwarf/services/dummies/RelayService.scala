package net.orfjackal.dimdwarf.services.dummies

import com.google.inject.Inject
import net.orfjackal.dimdwarf.controller.Hub
import net.orfjackal.dimdwarf.mq.MessageSender
import net.orfjackal.dimdwarf.services._

@ServiceScoped
class RelayService @Inject()(@Hub toHub: MessageSender[Any], spy: Spy) extends Service {
  def process(message: Any) {
    message match {
      case MessageToController(m) => {
        spy.log("service forwarded " + m)
        toHub.send(m)
      }
      case m => spy.log("service got " + m)
    }
  }
}
