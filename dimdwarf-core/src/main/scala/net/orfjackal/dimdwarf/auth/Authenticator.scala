package net.orfjackal.dimdwarf.auth

trait Authenticator {
  def isUserAuthenticated(onNo: => Unit)
}
