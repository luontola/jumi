package net.orfjackal.dimdwarf.auth

abstract sealed class AuthenticatorMessage

case class IsUserAuthenticated(credentials: Credentials)
case class YesUserIsAuthenticated() // TODO: identify the user
case class NoUserIsNotAuthenticated() // TODO: identify the user
