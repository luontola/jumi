---
title: Jumi Actors
layout: page
group: navigation
---
{% include JB/setup %}

Jumi has its own little [actors](http://en.wikipedia.org/wiki/Actor_model) library to support concurrency and asynchronous event-driven programming. But since it's such a cool little actors library, also other projects might want to use it. Jumi Actors is written in Java and has no dependencies.

The first release will be in Maven Central very soon. Until then you can build the [jumi-actors](https://github.com/orfjackal/jumi/tree/master/jumi-actors) module yourself.


Example
-------

Given the interface:

<pre class="brush: java">
    public interface Greeter {
        void sayGreeting(String name);
    }
</pre>

When we run the code:

<pre class="brush: java">
    ExecutorService actorsThreadPool = Executors.newCachedThreadPool();
    Actors actors = new MultiThreadedActors(
            actorsThreadPool,
            new DynamicEventizerProvider(),
            new CrashEarlyFailureHandler(),
            new NullMessageListener()
    );
    ActorThread actorThread = actors.startActorThread();

    ActorRef&lt;Greeter> helloSayer = actorThread.bindActor(Greeter.class, new Greeter() {
        public void sayGreeting(String name) {
            System.out.println("Hello " + name + " from " + Thread.currentThread().getName());
        }
    });
    helloSayer.tell().sayGreeting("World");
    System.out.println("Wazzup from " + Thread.currentThread().getName());

    actorThread.stop();
    actorsThreadPool.shutdown();
</pre>

Then it will print:

<pre class="brush: plain">
    Wazzup from main
    Hello World from pool-1-thread-1
</pre>

For some explanations and more information, see [the examples package](https://github.com/orfjackal/jumi/tree/master/jumi-actors/src/test/java/fi/jumi/actors/examples).


Features
--------

- **Natural to Java** - To send messages, you just create your own interface and call its methods. No need to create lots of small classes for individual messages, which would be easy with Scala's case classes and pattern matching, but not in Java.

- **Statically Typed** - Due to using Java interfaces, all messages are type checked and the actors are guaranteed to handle all messages (i.e. method calls) which can be sent to them.

- **Decoupled** - The actors themselves are not infected by the actors library. No implementation inheritance, global state or other invasive anti-patterns. Not even annotations. Unit testing without the container is easy.

- **Small** - As of 2012-04-30, the runtime JAR is only 26 KB <small>(and could be made up to 50% smaller by removing optional classes)</small>, which is minuscule compared to for example [Akka 2.0.1](http://akka.io/)'s minimum of 1.75 MB <small>(akka-actor.jar)</small> + 8.43 MB <small>(scala-library.jar)</small>. Naturally this means that it has less features, so if you are building distributed high-availablity systems then maybe Akka suits you better.

- **Many Actors per Thread** - Creating lots of actors is cheap, because individual actors don't require their own threads.

- **One Thread per Actor** - Each actor will stay in the same thread all its life. You have explicit control over which actors are run in which thread. This makes it even safe to share some mutable state between closely related actors (such as anonymous inner classes).

- **Garbage Collected Actors** - After nobody can send messages to an actor, it will be garbage collected the same way as normal Java objects. Note however that the lifecycle of actor *threads* must be managed manually.

- **Deterministic Testing** - Jumi Actors has [a single-threaded implementation](https://github.com/orfjackal/jumi/blob/master/jumi-actors/src/main/java/fi/jumi/actors/SingleThreadedActors.java) which is useful for integration testing actors with the container (unit tests should be written without any containers).

- **No Reflection** (optional) - It's possible to avoid reflection by using [jumi-actors-maven-plugin](https://github.com/orfjackal/jumi/tree/master/jumi-actors-maven-plugin) to generate the necessary event stubs at build time. Though using reflection requires less setup and thus might be preferable at least in tests.
