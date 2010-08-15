package net.orfjackal.dimdwarf.mq

trait MessageReceiver[T] {
  def take(): T

  def poll(): T
}
