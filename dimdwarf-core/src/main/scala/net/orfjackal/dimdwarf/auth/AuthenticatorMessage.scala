package net.orfjackal.dimdwarf.auth

abstract sealed class AuthenticatorMessage

case class IsUserAuthenticated(credentials: Credentials) extends AuthenticatorMessage
case class YesUserIsAuthenticated(credentials: Credentials) extends AuthenticatorMessage
case class NoUserIsNotAuthenticated(credentials: Credentials) extends AuthenticatorMessage
