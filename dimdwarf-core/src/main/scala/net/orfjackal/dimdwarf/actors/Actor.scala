package net.orfjackal.dimdwarf.actors

trait Actor {
  def start()

  def process(message: Any)
}
