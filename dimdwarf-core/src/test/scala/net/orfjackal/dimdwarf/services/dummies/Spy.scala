package net.orfjackal.dimdwarf.services.dummies

import com.google.inject.Singleton
import java.util.concurrent._

@Singleton
class Spy {
  private val messages = new LinkedBlockingQueue[String]

  def log(message: String) {
    messages.add(message)
  }

  def nextMessage(): String = {
    messages.poll(1000, TimeUnit.MILLISECONDS)
  }
}
