package net.orfjackal.dimdwarf.mq

trait MessageSender[T] {
  def send(message: T): Unit
}
