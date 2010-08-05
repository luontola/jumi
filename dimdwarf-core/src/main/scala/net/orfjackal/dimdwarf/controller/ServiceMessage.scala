package net.orfjackal.dimdwarf.controller

import net.orfjackal.dimdwarf.mq._

abstract sealed class ServiceMessage

case class RegisterNetworkService(toService: MessageSender[Any])
