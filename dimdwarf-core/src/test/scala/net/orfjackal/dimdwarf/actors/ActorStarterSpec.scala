package net.orfjackal.dimdwarf.actors

import org.junit.runner.RunWith
import net.orfjackal.specsy._
import org.scalatest.matchers.ShouldMatchers
import net.orfjackal.dimdwarf.util.StubProvider._
import java.util.HashSet
import java.util.concurrent.ConcurrentHashMap

@RunWith(classOf[Specsy])
class ActorStarterSpec extends Spec with ShouldMatchers {
  "When actors are started" >> {
    val threadOfActor = new ConcurrentHashMap[String, Thread]
    val actorA = makeActor("Actor A", {threadOfActor.put("A", Thread.currentThread)})
    val actorB = makeActor("Actor B", {threadOfActor.put("B", Thread.currentThread)})
    val actors = asSet(actorA, actorB)

    val starter = new SilentlyStoppableActorStarter(actors)
    starter.start()
    defer {starter.stop()}

    "the starter waits for the start() method of actors to finish execution" >> {
      threadOfActor.size should be(2) // look mom, no Thread.sleep()
    }
    val threadA = threadOfActor.get("A")
    val threadB = threadOfActor.get("B")
    "each actor runs in its own thread" >> {
      threadA should not be (null)
      threadB should not be (null)
      threadA should not be (threadB)
    }
    "the thread is named after the actor" >> {
      threadA.getName should be("Actor A")
      threadB.getName should be("Actor B")
    }
  }

  "Having two actors with the same name is not allowed" >> { // alternatively, add a sequential number to make them different
    val sameName = "Actor"

    val actor1 = makeActor(sameName, {})
    val actor2 = makeActor(sameName, {})
    val actors = asSet(actor1, actor2)

    evaluating {
      new ActorStarter(actors)
    } should produce[IllegalArgumentException]
  }


  def makeActor(name: String, onStart: => Unit): ActorRegistration = {
    new ActorRegistration(name,
      providerOf(new ActorContext(null)),
      providerOf(new ActorRunnable {
        def start() {
          onStart
        }

        def run() {}
      }))
  }

  def asSet(actors: ActorRegistration*): java.util.Set[ActorRegistration] = {
    val set = new HashSet[ActorRegistration]
    actors.foreach(set.add(_))
    set
  }
}
