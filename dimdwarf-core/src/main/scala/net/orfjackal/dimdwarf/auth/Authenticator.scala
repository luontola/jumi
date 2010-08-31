package net.orfjackal.dimdwarf.auth

trait Authenticator {
  def isUserAuthenticated(credentials: Credentials, onYes: => Unit, onNo: => Unit)
}
