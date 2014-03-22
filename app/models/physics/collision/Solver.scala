package models.physics.collision

import models.physics._
import models.physics.utils._

object Solver {

  def resolveCollision(a: Body, b: Body, mtd: V2)(implicit lastDelta: Double) : Body = {

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

      if(vn < World.gravity.y*ima*lastDelta*1000) {
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
    a
  }


}
