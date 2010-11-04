package net.orfjackal.dimdwarf.actors

import org.junit.runner.RunWith
import net.orfjackal.specsy._
import com.google.inject._
import collection.JavaConversions._
import org.scalatest.matchers.ShouldMatchers
import java.util.Set
import net.orfjackal.dimdwarf.actors.dummies._
import net.orfjackal.dimdwarf.controller._
import net.orfjackal.dimdwarf.modules._
import net.orfjackal.dimdwarf.mq._
import net.orfjackal.dimdwarf.context.ThreadContext

// TODO: use JUnit/Hamcrest matchers instead of Specs' matchers (?)
@RunWith(classOf[Specsy])
class InstallingActorsSpec extends Spec with ShouldMatchers {
  val injector = Guice.createInjector(new ActorInstallerModule(new ControllerModule, new RelayModule))

  val toHub = injector.getInstance(Key.get(new TypeLiteral[MessageSender[Any]] {}, classOf[Hub]))
  val spy = injector.getInstance(classOf[Spy])

  val starter = injector.getInstance(classOf[SilentlyStoppableActorStarter])
  starter.start()
  defer {starter.stop()}

  "Actor thread is named after the actor" >> {
    val actors = injector.getInstance(Key.get(new TypeLiteral[Set[ActorRegistration]] {}))
    val actorNames = actors.map(_.getName)

    actorNames should contain("Relay")
  }

  "Controllers can send messages to actors" >> {
    toHub.send(MessageToActor("message"))

    spy.nextMessage() should be("controller forwarded message")
    spy.nextMessage() should be("actor got message")
  }

  "Actors can send messages to controllers" >> {
    toHub.send(MessageToActor(MessageToController("message")))
    spy.nextMessage() // ignore the sending from controller to actor

    spy.nextMessage() should be("actor forwarded message")
    spy.nextMessage() should be("controller got message")
  }

  "Controllers must be ControllerScoped" >> {
    class NotControllerScopedController extends Controller {
      def process(message: Any) {}
    }

    evaluating {
      new ActorModule[Any]("Dummy") {
        def configure() {
          bindControllerTo(classOf[NotControllerScopedController])
        }
      }.configure()
    } should produce[IllegalArgumentException]
  }

  "Actors must be ActorScoped" >> {
    class NotActorScopedActor extends Actor[Any] {
      def start() {}

      def process(message: Any) {}
    }

    evaluating {
      new ActorModule[Any]("Dummy") {
        def configure() {
          bindActorTo(classOf[NotActorScopedActor])
        }
      }.configure()
    } should produce[IllegalArgumentException]
  }

  "Controllers can be dependant on other controllers, but their message queues are isolated" >> {
    // Test against the bug mentioned in
    // http://groups.google.com/group/google-guice/browse_thread/thread/5f61266829554993

    class DependantModule extends ActorModule[Any]("Dependant") {
      def configure() {
        bindControllerTo(classOf[DependantController])
        bindActorTo(classOf[DummyActor])
      }
    }
    class DependeeModule extends ActorModule[Any]("Dependee") {
      def configure() {
        bindControllerTo(classOf[DependeeController])
        bindActorTo(classOf[DummyActor])
      }
    }

    val injector = Guice.createInjector(new ActorInstallerModule(new DependantModule, new DependeeModule))
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
@ActorScoped
class DummyActor extends Actor[Any] {
  def start() {}

  def process(message: Any) {}
}
