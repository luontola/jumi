package net.orfjackal.dimdwarf.net

abstract sealed class ClientMessage

case class LoginRequest() extends ClientMessage

case class LoginFailure() extends ClientMessage
