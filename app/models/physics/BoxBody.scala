package models.physics

import models.physics.utils._

case class BoxBody(
    var position: V2
  , var velocity: V2
  , var acceleration: V2
  , size: V2
  , override val density: Float
  , override val static: Boolean
  , override val id: String = ""
) extends Body(density, static, id) {

  override def mass = size.x * size.y * density
  def topRight = position + size/2
  def topLeft = position + V2(-size.x, size.y)/2
  def bottomLeft = position - size/2
  def bottomRight = position + V2(size.x, -size.y)/2
}

object BoxBody {

}
