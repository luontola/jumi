package net.orfjackal.dimdwarf.mq

import org.junit.runner.RunWith
import net.orfjackal.specsy._
import org.junit.Assert._
import org.hamcrest.CoreMatchers._

@RunWith(classOf[Specsy])
class MessageQueueSpec extends Spec {
  val queue = new MessageQueue[String]

  "Messages are read from the queue in FIFO order" >> {
    queue.send("sent first")
    queue.send("sent second")

    assertThat(queue.take(), is("sent first"))
    assertThat(queue.take(), is("sent second"))
  }

  "When the queue is empty, take() will wait until there is a message" >> {
    val asynchronousSend = new Thread(new Runnable {
      def run() {
        queue.send("async message")
      }
    })

    asynchronousSend.start()
    assertThat(queue.take(), is("async message"))
  }
}
