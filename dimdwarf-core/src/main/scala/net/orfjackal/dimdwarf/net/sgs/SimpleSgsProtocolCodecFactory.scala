package net.orfjackal.dimdwarf.net.sgs

import org.apache.mina.core.session.IoSession
import org.apache.mina.filter.codec._

class SimpleSgsProtocolCodecFactory extends ProtocolCodecFactory {
  private val encoder = new SimpleSgsProtocolEncoder
  private val decoder = new SimpleSgsProtocolDecoder

  def getEncoder(session: IoSession): ProtocolEncoder = encoder

  def getDecoder(session: IoSession): ProtocolDecoder = decoder
}
