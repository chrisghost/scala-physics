package models.physics.utils

import play.api.libs.json._

case class V2(x: Float, y: Float) {
  def +(v: V2) = V2(v.x+x, v.y+y)
  def -(v: V2) = V2(x-v.x, y-v.y)
  def /(a: Int) = V2(x/a, y/a)
  def *(a: Int) = V2(x*a, y*a)
  def *(a: Float) = V2(x*a, y*a)
}

object V2 {

  implicit val v2Format = Json.format[V2]

}
