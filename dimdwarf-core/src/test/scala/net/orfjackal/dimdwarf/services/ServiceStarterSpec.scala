package net.orfjackal.dimdwarf.services

import org.junit.runner.RunWith
import net.orfjackal.specsy._
import org.scalatest.matchers.ShouldMatchers
import net.orfjackal.dimdwarf.util.StubProvider._
import java.util.HashSet
import java.util.concurrent.ConcurrentHashMap

@RunWith(classOf[Specsy])
class ServiceStarterSpec extends Spec with ShouldMatchers {
  "When services are started" >> {
    val threadOfService = new ConcurrentHashMap[String, Thread]

    val serviceA = makeService("Service A", {
      threadOfService.put("A", Thread.currentThread)
    })
    val serviceB = makeService("Service B", {
      threadOfService.put("B", Thread.currentThread)
    })

    val starter = new ServiceStarter(asSet(serviceA, serviceB)) {
      override def configureThread(t: Thread) {
        t.setUncaughtExceptionHandler(new HideInterruptedExceptions)
      }
    }
    starter.start()
    Thread.sleep(500) // TODO: create a start method and wait for its completion
    starter.stop()

    val threadA = threadOfService.get("A")
    val threadB = threadOfService.get("B")
    "each service runs in its own thread" >> {
      threadA should not be (null)
      threadB should not be (null)
      threadA should not be (threadB)
    }
    "the thread is named after the service" >> {
      threadA.getName should be("Service A")
      threadB.getName should be("Service B")
    }
  }

  "Having two services with the same name is not allowed" >> { // alternatively, add a sequential number to make them different
    val sameName = "Service"

    val service1 = makeService(sameName, {})
    val service2 = makeService(sameName, {})
    val services = asSet(service1, service2)

    evaluating {
      new ServiceStarter(services)
    } should produce[IllegalArgumentException]
  }


  def makeService(name: String, action: => Unit): ServiceRegistration = {
    new ServiceRegistration(name,
      providerOf(new ServiceContext(null)),
      providerOf(new Runnable {
        def run() {
          action
        }
      }))
  }

  def asSet(services: ServiceRegistration*): java.util.Set[ServiceRegistration] = {
    val set = new HashSet[ServiceRegistration]
    services.foreach(set.add(_))
    set
  }
}
