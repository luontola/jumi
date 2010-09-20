package net.orfjackal.dimdwarf.actors

import net.orfjackal.dimdwarf.mq.MessageReceiver
import org.slf4j.LoggerFactory
import com.google.inject.Inject

class ActorMessageLoop @Inject()(actor: Actor, toActor: MessageReceiver[_]) extends ActorRunnable {
  private val logger = LoggerFactory.getLogger(getClass)

  def start() {
    logger.debug("START: {}", actor.getClass.getName)
    actor.start()
  }

  def run() {
    while (true) {
      val message = toActor.take()
      logger.debug("PROCESS: {}", message)
      actor.process(message)
    }
  }
}
