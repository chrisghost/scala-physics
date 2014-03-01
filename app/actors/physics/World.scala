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

    println(collidingObjects)
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

  def collidingObjects = bodies.values.map { a =>
    (a.id, bodies.values.filter(b =>
      (b.id != a.id && collide(a, b))
    ).map(_.id))
  }

  def collide(a: Body, b: Body) = {
    (a, b) match {
      case (a:BoxBody, b:BoxBody) => {
        !(a.topRight.x < b.topLeft.x      ||//LEFT
          a.bottomRight.y > b.topRight.y  ||//UP
          a.topLeft.x > b.topRight.x      ||//RIGHT
          a.topLeft.y < b.bottomLeft.y)     //DOWN
      }
      case _ => false
    }
  }

  def genId = scala.util.Random.nextString(7)
}

