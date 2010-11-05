package net.orfjackal.dimdwarf.server

import org.junit.runner.RunWith
import net.orfjackal.specsy._
import com.google.inject._
import net.orfjackal.dimdwarf.auth._

@RunWith(classOf[Specsy])
class ModuleConfigurationSpec extends Spec {
  val port = 1000
  val appModule = new AbstractModule {
    def configure() {
      bind(classOf[CredentialsChecker[_]]).toInstance(new CredentialsChecker[Credentials] {
        def isValid(credentials: Credentials) = false
      })
    }
  }

  "DI configuration has no errors" >> {
    val modules = Main.configureServerModules(port, appModule)

    // throws CreationException if there is an error
    Guice.createInjector(modules)
  }
}
