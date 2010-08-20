package net.orfjackal.dimdwarf.services

import net.orfjackal.dimdwarf.mq.MessageReceiver
import org.slf4j.LoggerFactory

class ServiceRunner(service: Service, toService: MessageReceiver[Any]) extends Runnable {
  private val logger = LoggerFactory.getLogger(getClass)

  def run() {
    try {
      while (true) {
        val message = toService.take()
        logger.debug("PROCESS: {}", message)
        service.process(message)
      }
    } catch {
      case e =>
        logger.error("Internal error, service died", e)
        throw e
    }
  }
}
