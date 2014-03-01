package models.physics

import models.physics.utils._

case class BoxBody(
    var position: V2
  , var velocity: V2
  , var acceleration: V2
  , width: Float
  , height: Float
  , override val density: Float
  , override val static: Boolean
  , override val id: String = ""
) extends Body(density, static, id) {

  override def mass = width * height * density
}

object BoxBody {

}
