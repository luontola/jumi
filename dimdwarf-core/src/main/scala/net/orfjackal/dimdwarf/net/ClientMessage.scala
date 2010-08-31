package net.orfjackal.dimdwarf.net

abstract sealed class ClientMessage

case class LoginRequest(username: String, password: String) extends ClientMessage
case class LoginSuccess() extends ClientMessage
case class LoginFailure() extends ClientMessage
