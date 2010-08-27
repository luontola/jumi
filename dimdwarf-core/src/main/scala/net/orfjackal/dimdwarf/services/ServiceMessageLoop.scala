package net.orfjackal.dimdwarf.services

import net.orfjackal.dimdwarf.mq.MessageReceiver
import org.slf4j.LoggerFactory
import com.google.inject.Inject

class ServiceMessageLoop @Inject()(service: Service, toService: MessageReceiver[_]) extends ServiceRunnable {
  private val logger = LoggerFactory.getLogger(getClass)

  def start() {
    logger.debug("START: {}", service.getClass.getName)
    service.start()
  }

  def run() {
    while (true) {
      val message = toService.take()
      logger.debug("PROCESS: {}", message)
      service.process(message)
    }
  }
}
