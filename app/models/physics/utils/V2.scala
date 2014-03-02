package models.physics.utils

import play.api.libs.json._

case class V2(x: Double, y: Double) {
  def +(v: V2) = V2(v.x+x, v.y+y)
  def -(v: V2) = V2(x-v.x, y-v.y)
  def /(a: Double) = V2(x/a, y/a)
  def *(a: Double) = V2(x*a, y*a)
  def divides(a: Double) = V2(a/x, a/y)
  def dot(v: V2) = x*v.x+y*v.y
  def length = math.sqrt(x*x + y*y)
  def norm = this/length
}

object V2 {

  implicit val v2Format = Json.format[V2]

}
