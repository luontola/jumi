package net.orfjackal.dimdwarf.mq

import java.util.concurrent._

class MessageQueue[T] extends MessageSender[T] with MessageReceiver[T] {
  private val queue = new LinkedBlockingQueue[T]

  def send(message: T) {
    queue.put(message)
  }

  def take(): T = {
    queue.take()
  }

  def poll(): T = {
    queue.poll()
  }

  def poll(timeoutMillis: Long): T = {
    queue.poll(timeoutMillis, TimeUnit.MILLISECONDS)
  }
}
