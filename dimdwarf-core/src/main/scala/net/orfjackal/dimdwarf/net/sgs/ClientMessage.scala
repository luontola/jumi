package net.orfjackal.dimdwarf.net.sgs

// TODO: move these out of the sgs package, decouple them from the SimpleSgsProtocol?
abstract sealed class ClientMessage

case class LoginRequest(username: String, password: String) extends ClientMessage
case class LoginSuccess() extends ClientMessage
case class LoginFailure() extends ClientMessage
// TODO: LOGIN_REDIRECT

// TODO: SUSPEND*
// TODO: RESUME*
// TODO: RELOCATE*
// TODO: RECONNECT*

// TODO: SESSION_MESSAGE

case class LogoutRequest() extends ClientMessage
case class LogoutSuccess() extends ClientMessage

// TODO: CHANNEL*
