package com.rockthejvm.bookings.playground

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import scala.concurrent.duration._

object Playground {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem(Behaviors.setup[String] { ctx =>
      ctx.log.info("Welcome to Akka!")
      Behaviors.empty
    }, "SimpleSystem")

    import system.executionContext
    system.scheduler.scheduleOnce(3.seconds, () => system.terminate())
  }
}
