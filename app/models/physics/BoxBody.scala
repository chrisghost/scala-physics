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
) extends Body(density, static)

object BoxBody {

}
