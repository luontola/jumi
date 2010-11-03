package net.orfjackal.dimdwarf.actors

trait Actor {
  def start()

  // TODO: parameterize the message type
  def process(message: Any)
}
