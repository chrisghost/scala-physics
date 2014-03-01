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
case class Bodies()

class World extends Actor {
  val gravity = V2(0, -9.81f)
  val bodies : scala.collection.mutable.Map[String,Body] =  scala.collection.mutable.Map.empty
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
        case "" => {
          val id = genId
          n.body match {
            case bb: BoxBody => bodies(id) = bb.copy(id=id)
          }
        }
        case _ => bodies(n.body.id) = n.body
      }
    }
    case GetBodies => {
      //println(bodies.values)
      sender ! bodies.toMap
    }
  }

  def step(delta: Long) = {
    //println("step", delta)

    val d = delta/1000f // Use seconds for calculation inside the physics engine

    applyGravity(d)

    eulerIntegration(d)

    val cc = collidingObjects
    if(cc.size > 0)
      println(cc)
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

  def resolveCollision(a: Body, b: Body) = {

  }

  def collidingObjects = bodies.values.map { a =>
    (
      a,
      bodies.values.filter(_.id != a.id)
      .filter(b => collide(a, b))
    )
  }.filterNot(_._2 == Nil)

  def collide(a: Body, b: Body) = {
    (a, b) match {
      case (a:BoxBody, b:BoxBody) => {
        !(a.topLeft.x > b.topRight.x      ||//LEFT
          a.topLeft.y < b.bottomLeft.y    ||//UP
          a.topRight.x < b.topLeft.x      ||//RIGHT
          a.bottomLeft.y > b.topLeft.y)     //DOWN
      }
      case _ => {
        false
      }
    }
  }

  def genId = scala.util.Random.nextInt(10000).toString
}

