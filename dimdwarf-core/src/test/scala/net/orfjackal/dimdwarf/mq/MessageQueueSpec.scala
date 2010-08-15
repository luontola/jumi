package net.orfjackal.dimdwarf.mq

import org.junit.runner.RunWith
import net.orfjackal.specsy._
import org.specs.SpecsMatchers

@RunWith(classOf[Specsy])
class MessageQueueSpec extends Spec with SpecsMatchers {
  val queue = new MessageQueue[String]

  "Messages are read from the queue in FIFO order" >> {
    queue.send("sent first")
    queue.send("sent second")

    queue.take() must_== "sent first"
    queue.take() must_== "sent second"
  }

  "When the queue is empty, take() will wait until there is a message" >> {
    val asynchronousSend = new Thread(new Runnable {
      def run() {
        queue.send("async message")
      }
    })

    asynchronousSend.start()
    queue.take() must_== "async message"
  }

  "When the queue is empty, poll() will return immediately" >> {
    queue.send("message")
    queue.poll() must_== "message"

    queue.poll() must beNull
  }
}
