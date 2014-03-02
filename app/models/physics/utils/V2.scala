package models.physics.utils

import play.api.libs.json._

case class V2(x: Double, y: Double) {
  def ==(v: V2) = (v.x == x && v.y == y)
  def +(v: V2) = V2(v.x+x, v.y+y)
  def -(v: V2) = V2(x-v.x, y-v.y)
  def unary_- = V2(-x, -y)
  def /(a: Double) = V2(x/a, y/a)
  def *(a: Double) = V2(x*a, y*a)
  def *(v: V2) = V2(x*v.x, y*v.y)
  def divides(a: Double) = V2(a/x, a/y)
  def dot(v: V2) = x*v.x+y*v.y
  def length = math.sqrt(x*x + y*y)
  def dst(v: V2) = math.sqrt(math.pow(x-v.x, 2) + math.pow(y-v.y, 2))
  def norm = this/length
  def inverse = V2(y, x)
}

object V2 {

  implicit val v2Format = Json.format[V2]

}
