package net.orfjackal.dimdwarf.net

abstract sealed class NetworkMessage

case class ReceivedFromClient(message: ClientMessage)
case class SendToClient(message: ClientMessage)
