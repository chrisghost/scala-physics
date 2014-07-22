package actors.physics

import akka.actor._
import akka.pattern.ask

import actors._
import models.physics._
import models.physics.collision._
import models.physics.utils._

case class NewBody(body: Body)
case object CleanActiveBodies
case object GetBodies
case class FakeTick(d: Long)
case class Tick()
case class Bodies()

class WorldProcessor extends Actor {
  val bodies : scala.collection.mutable.Map[String,Body] =  scala.collection.mutable.Map.empty
  var lastTick: Long = scala.compat.Platform.currentTime
  var lastDelta: Double = 0.0
  def receive = {
    case t:FakeTick => {
      step(t.d)
    }
    case Tick => {
      val curTime = scala.compat.Platform.currentTime
      lastDelta = (curTime - lastTick)/1000f // Use seconds for calculation inside the physics engine
      lastTick = curTime
      step(lastDelta)
    }

    case n:NewBody => {
      n.body.id match {
        case "" => {
          val id = genId
          n.body match {
            case bb: BoxBody => bodies(id) = bb.copy(id=id)
            case cb: CircleBody => bodies(id) = cb.copy(id=id)
          }
        }
        case _ => bodies(n.body.id) = n.body
      }
    }
    case CleanActiveBodies => {
      val toDel = bodies.filterNot(_._2.static).keys
      bodies.retain((k, e) => e.static)
      sender ! toDel.toList
    }
    case GetBodies => {
      //println(bodies.values)
      sender ! bodies.toMap
    }
  }

  def step(d: Double) = {
    //println("step", delta)

    applyGravity(d)

    eulerIntegration(d)

    val cc = collidingObjects
    if(cc.size > 0) {
      cc.map{ e =>
        e._2.toList
          .sortWith((e1, e2) => (!e1.static))
          .map { body =>
            Detector.processCollision(e._1, body)(lastDelta).map { a =>
              bodies(a.id) = a
            }
          }
      }
    }
  }
  def eulerIntegration(d: Double) {
    bodies.values.filterNot(_.static).foreach { b =>
      b.position = b.position + (b.velocity * d)
      b.velocity = b.velocity + (b.acceleration * d)
    }
  }

  def applyGravity(d: Double) {
    bodies.values.filterNot(_.static).foreach { b =>
      b.acceleration = b.acceleration + (World.gravity * b.mass * d)
    }
  }


  def collidingObjects = bodies.values.map { a =>
    (
      a,
      bodies.values.filter(_.id != a.id)
      .filter(b => collide(a, b))
    )
  }.filterNot(e => (e._2 == Nil || e._1.static))

  def collide(a: Body, b: Body)  = {
    (a, b) match {
      case (a:BoxBody, b:BoxBody) => {
        !(a.topLeft.x > b.topRight.x      ||//LEFT
          a.topLeft.y < b.bottomLeft.y    ||//UP
          a.topRight.x < b.topLeft.x      ||//RIGHT
          a.bottomLeft.y > b.topLeft.y)     //DOWN
      }
      case (a: CircleBody, b: CircleBody) => {
        var r = a.radius + b.radius
        r > a.position.dst(b.position)
        //(math.pow(a.position.x + b.position.x, 2) + math.pow(a.position.y + b.position.y, 2))
      }
      case (a: CircleBody, b:BoxBody) => Detector.AABBandCircleCollide(a, b)
      case (a: BoxBody, b:CircleBody) => Detector.AABBandCircleCollide(b, a)
      case _ => {
        false
      }
    }
  }

  def genId = scala.util.Random.nextInt(10000).toString

  def clamp(min: Double, max: Double, v: Double) =
    if (v < min) min
    else if (v > max) max
    else v

}

