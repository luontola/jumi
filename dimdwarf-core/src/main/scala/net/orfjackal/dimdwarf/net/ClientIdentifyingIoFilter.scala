package net.orfjackal.dimdwarf.net

import org.apache.mina.core.filterchain.IoFilterAdapter
import org.apache.mina.core.session.IoSession
import org.apache.mina.core.filterchain.IoFilter.NextFilter

class ClientIdentifyingIoFilter extends IoFilterAdapter {
  override def messageReceived(nextFilter: NextFilter, session: IoSession, message: AnyRef) {
    nextFilter.messageReceived(session, ReceivedFromClient(message.asInstanceOf[ClientMessage]))
  }
}
