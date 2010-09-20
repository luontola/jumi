package net.orfjackal.dimdwarf.actors.dummies

abstract sealed class RelayMessage

case class MessageToActor(message: Any) extends RelayMessage
case class MessageToController(message: Any) extends RelayMessage
