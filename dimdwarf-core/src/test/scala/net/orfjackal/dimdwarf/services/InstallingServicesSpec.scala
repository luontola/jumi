package net.orfjackal.dimdwarf.services

import org.junit.runner.RunWith
import net.orfjackal.specsy._
import com.google.inject._
import collection.JavaConversions._
import org.scalatest.matchers.ShouldMatchers
import java.util.Set
import net.orfjackal.dimdwarf.services.dummies._
import net.orfjackal.dimdwarf.controller._
import scala.collection.mutable.Buffer
import net.orfjackal.dimdwarf.modules._
import net.orfjackal.dimdwarf.mq._

@RunWith(classOf[Specsy])
class InstallingServicesSpec extends Spec with ShouldMatchers {
  val injector = Guice.createInjector(new ServiceInstallerModule(new ControllerModule, new RelayModule))

  val toHub = injector.getInstance(Key.get(new TypeLiteral[MessageSender[Any]] {}, classOf[Hub]))
  val spy = injector.getInstance(classOf[Spy])

  // TODO: extract starting services to its own class (and stopping - for tests only) 
  val services = injector.getInstance(Key.get(new TypeLiteral[Set[ServiceRegistration]] {}))
  val threads = Buffer[Thread]()
  services.foreach(service => {
    println("Starting service " + service.getName)
    val t = new Thread(new ServiceRunner(service), service.getName)
    t.start()
    threads.add(t)
  })
  defer {
    threads.foreach(t => {
      t.interrupt()
      t.join()
    })
  }

  "Service thread is named after the service" >> {
    val serviceNames = services.map(_.getName)

    //assertThat(serviceNames, hasItem("Relay")) // TODO: doesn't compile, create a Scala wrapper
    //assertTrue(serviceNames.toString, serviceNames.contains("Relay"))
    serviceNames should contain("Relay") // TODO: evaluate the different matchers
  }

  "Controllers can send messages to services" >> {
    toHub.send(MessageToService("message"))

    spy.nextMessage() should be("controller forwarded message")
    spy.nextMessage() should be("service got message")
  }

  "Services can send messages to controllers" >> {
    toHub.send(MessageToService(MessageToController("message")))
    spy.nextMessage() // ignore the sending from controller to service

    spy.nextMessage() should be("service forwarded message")
    spy.nextMessage() should be("controller got message")
  }

  // TODO
  "Controllers of one module can access controllers of other modules" // make Bar depend on Foo
  "Other modules cannot directly access the services" // use a nested PrivateModule
  "Other modules cannot directly access the message queues" // use a nested PrivateModule
  "The main thread can know when the services have started up" // add a start() method to the Service trait
  "Controllers do not necessarily need to be backed by a service"
}
