package net.orfjackal.dimdwarf.services

import net.orfjackal.dimdwarf.mq.MessageReceiver
import org.slf4j.LoggerFactory
import com.google.inject.Inject

class ServiceMessageLoop @Inject()(service: Service, toService: MessageReceiver[_]) extends Runnable {
  private val logger = LoggerFactory.getLogger(getClass)

  def run() {
    try {
      logger.debug("STARTED: {}", service.getClass)
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
