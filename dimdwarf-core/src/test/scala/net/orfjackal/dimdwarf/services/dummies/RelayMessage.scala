package net.orfjackal.dimdwarf.services.dummies

abstract sealed class RelayMessage

case class MessageToService(message: Any) extends RelayMessage
case class MessageToController(message: Any) extends RelayMessage
