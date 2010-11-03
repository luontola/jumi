package net.orfjackal.dimdwarf.actors

trait Actor[T] {
  def start()

  def process(message: T)
}
