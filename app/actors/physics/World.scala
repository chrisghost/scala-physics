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
    if(cc.size > 0) {
      cc.map{ e =>
        (e._1, e._2.head) match {
          case (a:BoxBody, b:BoxBody) =>
            minimumTranslation(a, b)
              .map { r =>
                println(r)
                resolveCollision(a, b, r)
              }
        }
      }
    }
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

  def resolveCollision(a: Body, b: Body, mtd: V2) = {
    a.position = a.position + mtd
    val rest = -(1-math.min(a.restitution, b.restitution))
    if(mtd.x == 0) {
      if(!(a.velocity.y<0 && mtd.y<0))
        a.velocity = a.velocity.copy(y= rest*a.velocity.y)
    } else {
      if(!(a.velocity.x<0 && mtd.x<0))
        a.velocity = a.velocity.copy(x= rest*a.velocity.x)
    }
  }

  def minimumTranslation(a:BoxBody, b:BoxBody): Option[V2] = {
    val amin = a.bottomLeft
    val amax = a.topRight
    val bmin = b.bottomLeft
    val bmax = b.topRight

    var mtd = V2(0, 0)

    val left = (bmin.x - amax.x)
    val right = (bmax.x - amin.x)
    val top = (bmin.y - amax.y)
    val bottom = (bmax.y - amin.y)

    // box dont intersect
    if (left > 0 || right < 0) return None
    if (top > 0 || bottom < 0) return None

    // box intersect. work out the mtd on both x and y axes.
    if (math.abs(left) < right)
      mtd = V2(left, 0)
    else
      mtd = V2(right, 0)

    if (math.abs(top) < bottom)
      mtd = V2(mtd.x, top)
    else
      mtd = V2(mtd.x, bottom)

    // 0 the axis with the largest mtd value.
    if (math.abs(mtd.x) < math.abs(mtd.y))
      mtd = V2(mtd.x, 0)
    else
      mtd = V2(0, mtd.y)

    Some(mtd)
  }

  def collidingObjects = bodies.values.map { a =>
    (
      a,
      bodies.values.filter(_.id != a.id)
      .filter(b => collide(a, b))
    )
  }.filterNot(e => (e._2 == Nil || e._1.static))

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

