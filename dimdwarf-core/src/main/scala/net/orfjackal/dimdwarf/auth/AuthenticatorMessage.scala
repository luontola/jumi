package net.orfjackal.dimdwarf.auth

abstract sealed class AuthenticatorMessage

case class IsUserAuthenticated(credentials: Credentials)
case class YesUserIsAuthenticated(credentials: Credentials)
case class NoUserIsNotAuthenticated(credentials: Credentials)
