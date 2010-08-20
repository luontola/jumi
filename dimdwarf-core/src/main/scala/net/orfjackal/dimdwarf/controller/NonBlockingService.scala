package net.orfjackal.dimdwarf.controller

trait NonBlockingService {
  def process(message: Any)
}
