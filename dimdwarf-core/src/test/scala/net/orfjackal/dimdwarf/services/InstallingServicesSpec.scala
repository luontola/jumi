package net.orfjackal.dimdwarf.services

import org.junit.runner.RunWith
import net.orfjackal.specsy._
import com.google.inject._
import collection.JavaConversions._
import org.scalatest.matchers.ShouldMatchers
import java.util.Set
import net.orfjackal.dimdwarf.services.dummies._
import net.orfjackal.dimdwarf.controller._
import net.orfjackal.dimdwarf.modules._
import net.orfjackal.dimdwarf.mq._
import net.orfjackal.dimdwarf.context.ThreadContext

@RunWith(classOf[Specsy])
class InstallingServicesSpec extends Spec with ShouldMatchers {
  val injector = Guice.createInjector(new ServiceInstallerModule(new ControllerModule, new RelayModule))

  val toHub = injector.getInstance(Key.get(new TypeLiteral[MessageSender[Any]] {}, classOf[Hub]))
  val spy = injector.getInstance(classOf[Spy])

  val starter = injector.getInstance(classOf[SilentlyStoppableServiceStarter])
  starter.start()
  defer {starter.stop()}

  "Service thread is named after the service" >> {
    val services = injector.getInstance(Key.get(new TypeLiteral[Set[ServiceRegistration]] {}))
    val serviceNames = services.map(_.getName)

    serviceNames should contain("Relay")
    //assertThat(serviceNames, hasItem("Relay")) // TODO: doesn't compile, create a Scala wrapper
    //assertTrue(serviceNames.toString, serviceNames.contains("Relay"))
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

  "Controllers must be ControllerScoped" >> {
    class NotControllerScopedController extends Controller {
      def process(message: Any) {}
    }

    evaluating {
      new ServiceModule("Dummy") {
        def configure() {
          bindControllerTo(classOf[NotControllerScopedController])
        }
      }.configure()
    } should produce[IllegalArgumentException]
  }

  "Services must be ServiceScoped" >> {
    class NotServiceScopedService extends Service {
      def start() {}

      def process(message: Any) {}
    }

    evaluating {
      new ServiceModule("Dummy") {
        def configure() {
          bindServiceTo(classOf[NotServiceScopedService])
        }
      }.configure()
    } should produce[IllegalArgumentException]
  }

  "Controllers can be dependant on other controllers, but their message queues are isolated" >> {
    // Test against the bug mentioned in
    // http://groups.google.com/group/google-guice/browse_thread/thread/5f61266829554993

    class DependantModule extends ServiceModule("Dependant") {
      def configure() {
        bindControllerTo(classOf[DependantController])
        bindMessageQueueOfType(classOf[Any])
      }

      @Provides def controllerRegistration(controller: Provider[Controller]) = new ControllerRegistration(serviceName, controller)
    }
    class DependeeModule extends ServiceModule("Dependee") {
      def configure() {
        bindControllerTo(classOf[DependeeController])
        bindMessageQueueOfType(classOf[Any])
      }

      @Provides def controllerRegistration(controller: Provider[Controller]) = new ControllerRegistration(serviceName, controller)
    }

    val injector = Guice.createInjector(new ServiceInstallerModule(new DependantModule, new DependeeModule))
    val controllers = injector.getInstance(Key.get(new TypeLiteral[Set[ControllerRegistration]] {}))
    val dependantProvider = controllers.filter(_.getName == "Dependant").head.getController
    val dependeeProvider = controllers.filter(_.getName == "Dependee").head.getController

    def eachControllerGetsItsPersonalMessageQueue(dependant: DependantController, dependee: DependeeController) {
      val injectedDependee = dependant.dependee
      dependant.queue should not be (injectedDependee.queue)
      dependee.queue should be(injectedDependee.queue)
    }

    "When dependant is instantiated first, each controller gets its personal message queue" >> {
      runInControllerContext(injector, {
        val dependant = dependantProvider.get.asInstanceOf[DependantController]
        val dependee = dependeeProvider.get.asInstanceOf[DependeeController]

        eachControllerGetsItsPersonalMessageQueue(dependant, dependee)
      })
    }
    "When dependee is instantiated first, each controller gets its personal message queue" >> {
      runInControllerContext(injector, {
        val dependee = dependeeProvider.get.asInstanceOf[DependeeController]
        val dependant = dependantProvider.get.asInstanceOf[DependantController]

        eachControllerGetsItsPersonalMessageQueue(dependant, dependee)
      })
    }
  }

  def runInControllerContext(injector: Injector, action: => Unit) {
    val context = injector.getInstance(classOf[ControllerContext])
    ThreadContext.runInContext(context, new Runnable {
      def run() {
        action
      }
    })
  }
}

@ControllerScoped
class DependantController @Inject()(val dependee: DependeeController, val queue: MessageSender[Any]) extends Controller {
  def process(message: Any) {}
}
@ControllerScoped
class DependeeController @Inject()(val queue: MessageSender[Any]) extends Controller {
  def process(message: Any) {}
}
