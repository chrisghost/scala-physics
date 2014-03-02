package models.physics

import models.physics.utils._
import play.api.libs.json._

case class BoxBody(
    var position: V2
  , var velocity: V2
  , var acceleration: V2
  , size: V2
  , override val density: Float
  , override val restitution: Float
  , override val static: Boolean
  , override val id: String = ""
) extends Body(density, restitution, static, id) {

  override def mass = size.x * size.y * density
  override val invMass = 1/mass
  def topRight = position + size/2
  def topLeft = position + V2(-size.x, size.y)/2
  def bottomLeft = position - size/2
  def bottomRight = position + V2(size.x, -size.y)/2
}

object BoxBody {

  implicit val boxBodyFormat = Json.format[BoxBody]

}
