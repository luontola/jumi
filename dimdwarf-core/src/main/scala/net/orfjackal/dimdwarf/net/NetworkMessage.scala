package net.orfjackal.dimdwarf.net

import net.orfjackal.dimdwarf.net.sgs.ClientMessage

abstract sealed class NetworkMessage

case class ReceivedFromClient(message: ClientMessage, session: SessionHandle) extends NetworkMessage
case class SendToClient(message: ClientMessage, session: SessionHandle) extends NetworkMessage
