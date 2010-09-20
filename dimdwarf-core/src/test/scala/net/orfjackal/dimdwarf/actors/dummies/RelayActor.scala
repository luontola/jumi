package net.orfjackal.dimdwarf.actors.dummies

import com.google.inject.Inject
import net.orfjackal.dimdwarf.controller.Hub
import net.orfjackal.dimdwarf.mq.MessageSender
import net.orfjackal.dimdwarf.actors._

@ActorScoped
class RelayActor @Inject()(@Hub toHub: MessageSender[Any], spy: Spy) extends Actor {
  def start() {
  }

  def process(message: Any) {
    message match {
      case MessageToController(m) => {
        spy.log("actor forwarded " + m)
        toHub.send(m)
      }
      case m => spy.log("actor got " + m)
    }
  }
}
