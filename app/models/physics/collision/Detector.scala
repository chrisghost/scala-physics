package models.physics.collision

import models.physics._
import models.physics.utils._

object Detector {
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

  }

  def closestPointToRectangle(v: V2, b: BoxBody) = {
      // relative position of v from the point b
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

  def processCollision(a:Body, b: Body)(implicit lastDelta: Double) : Option[Body] = {
    ((a, b) match {
      case (a:BoxBody, b:BoxBody) =>        minimumTranslation(a, b)
      case (a:CircleBody, b:CircleBody) =>  minimumTranslation(a, b)
      case (a:CircleBody, b:BoxBody) =>     minimumTranslation(a, b)//.map(_*V2(-1, 1))
      case (a:BoxBody, b:CircleBody) =>     minimumTranslation(b, a).map(_*V2(-1, -1))//.map(_.inverse)
    }).map { r => Solver.resolveCollision(a, b, r) }
  }

  def minimumTranslation(a:CircleBody, b:CircleBody): Option[V2] = {
    //println("===============", a, b)
    val n = b.position - a.position
    val r = math.pow(a.radius + b.radius, 2)
    val d = n.length
    var mtd = V2(0, 0)
    if(d != 0) {
      mtd = (n*r).norm*V2(-1, -1) // normalize n (optimisation to reuse sqrt made in .length)
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

  def minimumTranslation(a:CircleBody, b:BoxBody): Option[V2] = {
    val c = closestPointToRectangle(a.position, b)
    val d = (a.position - c)
    val dist = d.length
    Some((d * (a.radius - dist) / dist))
  }
}
