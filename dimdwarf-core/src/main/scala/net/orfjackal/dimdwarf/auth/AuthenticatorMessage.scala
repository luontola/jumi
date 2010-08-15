package net.orfjackal.dimdwarf.auth

abstract sealed class AuthenticatorMessage

case class IsUserAuthenticated()

case class YesUserIsAuthenticated()

case class NoUserIsNotAuthenticated()
