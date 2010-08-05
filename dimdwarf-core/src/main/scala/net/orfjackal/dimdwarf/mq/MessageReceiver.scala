package net.orfjackal.dimdwarf.mq

trait MessageReceiver[T] {
  def take(): T
}
