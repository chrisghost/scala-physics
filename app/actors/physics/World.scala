package actors.physics

import akka.actor._
import akka.pattern.ask

import actors._
import models.physics._
import models.physics.utils._

case class NewBody(body: Body)
case object GetBodies
case class FakeTick(d: Long)
case class Tick()

class World extends Actor {
  val gravity = V2(0, -9.81f)
  val bodies : scala.collection.mutable.Map[String, Body] =  scala.collection.mutable.Map.empty
  var lastTick: Long = scala.compat.Platform.currentTime
  def receive = {
    case t:FakeTick => {
      step(t.d)
    }
    case Tick => {
      val curTime = scala.compat.Platform.currentTime
      val delta = curTime - lastTick
      lastTick = curTime
      step(delta)
    }

    case n:NewBody => {
      n.body.id match {
        case "" => bodies(genId) = n.body
        case _ => bodies(n.body.id) = n.body
      }
    }
    case GetBodies => {
      sender ! bodies.toMap
    }
  }

  def step(delta: Long) = {
    println("world step", delta)

    val d = delta/1000f // Use seconds for calculation inside the physics engine

    applyGravity(d)

    eulerIntegration(d)
  }

  def eulerIntegration(d: Float) {
    bodies.values.filterNot(_.static).foreach { b =>
      b.position = b.position + (b.velocity * d)
      b.velocity = b.velocity + (b.acceleration * d)
    }
  }

  def applyGravity(d: Float) {
    bodies.values.filterNot(_.static).foreach { b =>
      b.acceleration = b.acceleration + (gravity * b.mass * d)
    }
  }

  def genId = scala.util.Random.nextString(7)
}

