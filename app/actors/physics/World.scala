package actors.physics

import akka.actor._
import akka.pattern.ask

import actors._
import models.physics._
import models.physics.utils._

case class NewBody(body: Body)
case object GetBodies
case class Tick()

class World extends Actor {
  val gravity : Float = 9.81f
  val bodies : scala.collection.mutable.Map[String, Body] =  scala.collection.mutable.Map.empty
  var lastTick: Long = scala.compat.Platform.currentTime
  def receive = {
    case Tick => {
      val curTime = scala.compat.Platform.currentTime
      val delta = curTime - lastTick
      lastTick = curTime
      step(delta)
    }

    case n:NewBody => {
      bodies(genId) = n.body
    }
    case GetBodies => {
      sender ! bodies.toMap
    }
  }

  def step(delta: Long) = {
    println("world step", delta)
    eulerIntegration(delta)
  }

  def eulerIntegration(delta: Long) {
    bodies.values.filterNot(_.static).foreach { b =>
      b.position = b.position + (b.velocity * delta)
      b.velocity = b.velocity + (b.acceleration * delta)
    }
  }

  def genId = scala.util.Random.nextString(7)
}

