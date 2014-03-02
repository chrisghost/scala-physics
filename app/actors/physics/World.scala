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
            processCollision(e._1, body)
          }
      }
    }
  }
  def processCollision(a:Body, b: Body) {
    ((a, b) match {
      case (a:BoxBody, b:BoxBody) =>        minimumTranslation(a, b)
      case (a:CircleBody, b:CircleBody) =>  minimumTranslation(a, b)
      case (a:CircleBody, b:BoxBody) =>     minimumTranslation(a, b).map(_*V2(-1, 1))
      case (a:BoxBody, b:CircleBody) =>     minimumTranslation(b, a).map(_*V2(-1, -1))//.map(_.inverse)
    }).map { r => resolveCollision(a, b, r) }
  }

  def eulerIntegration(d: Double) {
    bodies.values.filterNot(_.static).foreach { b =>
      b.position = b.position + (b.velocity * d)
      b.velocity = b.velocity + (b.acceleration * d)
    }
  }

  def applyGravity(d: Double) {
    bodies.values.filterNot(_.static).foreach { b =>
      b.acceleration = b.acceleration + (gravity * b.mass * d)
    }
  }

  def resolveCollision(a: Body, b: Body, mtd: V2) = {

    val ima = 1/a.mass
    val imb = 1/b.mass
    val im  = ima + imb

    //println(a.position, " = to => ", a.position + mtd * (ima / im))
    a.position = a.position + mtd * (ima / im)
    //if(!b.static) b.position = b.position - mtd * (imb / im)

    val n = mtd.norm
    val v = (a.velocity - b.velocity)
    val vn = v.dot(n)

    if(vn < 0.0) {
      val rest = math.min(a.restitution, b.restitution)

      val j = -(1.0f + rest) * vn / (im)

      if(vn < gravity.y*ima*lastDelta*1000) {
        a.velocity = a.velocity + n * (j * ima)
        a.acceleration = a.acceleration.copy(y=a.acceleration.y/2)
      } else {
        if(n.x == 0) {
          a.velocity = a.velocity.copy(y=0)
        a.acceleration = a.acceleration.copy(y=0)
        } else {
          a.velocity = a.velocity.copy(x=0)
          a.acceleration = a.acceleration.copy(x=0)
        }
      }
    }
  }

  def minimumTranslation(a:CircleBody, b:BoxBody): Option[V2] = {
    val c = closestPointToRectangle(a.position, b)
    val d = (a.position - c)
    val dist2 = d.dot(d)
    val dist = math.sqrt(dist2)
    Some((d * (a.radius - dist) / dist))
  }


/*
  def minimumTranslation(a:CircleBody, b:BoxBody): Option[V2] = {
    println("CIRCLE && BOX")
    val n = b.position - a.position
    var closest = n
    val x_extent = b.size.x
    val y_extent = b.size.y

    closest = V2(
      clamp(-x_extent, x_extent, closest.x)
    , clamp(-y_extent, y_extent, closest.y)
    )
    var inside = false

    if(n == closest) {
      inside = true

      // Find closest axis
      if(math.abs( n.x ) > math.abs( n.y ))
      {
        // Clamp to closest extent
        if(closest.x > 0)
          closest = V2(x_extent, closest.y)
        else
          closest = V2(-x_extent, closest.y)
      }
      // y axis is shorter
      else
      {
        // Clamp to closest extent
        if(closest.y > 0)
          closest = V2(closest.x, y_extent)
        else
          closest = V2(closest.x, -y_extent)
      }
    }
    val normal = n - closest
    var d = normal.length
    val r = a.radius

    // Early out of the radius is shorter than distance to closest point and
    // Circle not inside the AABB
    if(d > r * r && !inside) {
      None
    } else {

      // Avoided sqrt until we needed
      d = math.sqrt( d )

      // Collision normal needs to be flipped to point outside if circle was
      // inside the AABB
      println(n, -n, inside)
      if(inside)
      {
        Some(-n)
      }
      else
      {
        Some(n)
      }
    }

    None
  }
*/

  def minimumTranslation(a:CircleBody, b:CircleBody): Option[V2] = {
    //println("===============", a, b)
    val n = b.position - a.position
    val r = math.pow(a.radius + b.radius, 2)
    val d = n.length
    var mtd = V2(0, 0)
    if(d != 0) {
      mtd = (n /d)*V2(-1, -1) // normalize n (optimisation to reuse sqrt made in .length)
    } else {
      mtd = V2(1,1).norm
    }
    Some(mtd)
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
      case (a: CircleBody, b:BoxBody) => AABBandCircleCollide(a, b)
      case (a: BoxBody, b:CircleBody) => AABBandCircleCollide(b, a)
      case _ => {
        false
      }
    }
  }

  def AABBandCircleCollide(a: CircleBody, b: BoxBody) = {
    val c = closestPointToRectangle(a.position, b)

    // relative position of point from sphere centre
    val d = (a.position - c)

    // check if point inside sphere
    val dist2 = d.dot(d)
    if(dist2 >= a.radius*a.radius) false
    else {
      // minimum translation vector (vector of minimum intersection
      // that we can use to push the objects apart so they stop intersecting).
      val dist = math.sqrt(dist2)
      if(dist < 0.0000001f) false
      else {
        true
        //val mtd = d * (a.radius - dist) / dist
      }
    }

/*
      val circleDistance = V2(
          math.abs(a.position.x - b.position.x)
        , math.abs(a.position.y - b.position.y))

      if (circleDistance.x > (b.size.x/2 + a.radius)) false
      else if (circleDistance.y > (b.size.y/2 + a.radius)) false
      else if (circleDistance.x <= (b.size.x/2)) true
      else if (circleDistance.y <= (b.size.y/2)) true
      else {
        val cornerDistance_sq = math.pow(circleDistance.x - b.size.x/2, 2) + math.pow(circleDistance.y - b.size.y/2, 2)

        (cornerDistance_sq <= math.pow(a.radius, 2))
       }
*/
  }

  def closestPointToRectangle(v: V2, b: BoxBody) = {
      // relative position of p from the point 'p'
      var d = (v - b.position)

      // rectangle half-size
      val h = b.size/2

      // special case when the sphere centre is inside the rectangle
      if(math.abs(d.x) < h.x && math.abs(d.y) < h.y)
      {
          // use left or right side of the rectangle boundary
          // as it is the closest
          if((h.x - math.abs(d.x)) < (h.y - math.abs(d.y)))
          {
               d = V2(h.x * math.signum(d.x), 0.0f)
          }
          // use top or bottom side of the rectangle boundary
          // as it is the closest
          else
          {
               d = V2(0.0f, h.y * math.signum(d.y))
          }
      }
      else
      {
          // clamp to rectangle boundary
          if(math.abs(d.x) > h.x) d = d.copy(x = h.x * math.signum(d.x))
          if(math.abs(d.y) > h.y) d = d.copy(y = h.y * math.signum(d.y))
      }

      // the closest point on rectangle from p
      b.position + d
  }


  def genId = scala.util.Random.nextInt(10000).toString

  def clamp(min: Double, max: Double, v: Double) =
    if (v < min) min
    else if (v > max) max
    else v

}

