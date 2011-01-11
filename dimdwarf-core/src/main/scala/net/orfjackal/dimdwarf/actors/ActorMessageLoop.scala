package net.orfjackal.dimdwarf.actors

import net.orfjackal.dimdwarf.mq.MessageReceiver
import org.slf4j.LoggerFactory
import javax.inject.Inject

class ActorMessageLoop[T] @Inject()(actor: Actor[T], toActor: MessageReceiver[T]) extends ActorRunnable {
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
