package net.orfjackal.dimdwarf.actors.dummies

import java.util.concurrent._
import javax.inject.Singleton

@Singleton
class Spy {
  private val messages = new LinkedBlockingQueue[String]

  def log(message: String) {
    messages.add(message)
  }

  def nextMessage(): String = {
    messages.take()
  }
}
