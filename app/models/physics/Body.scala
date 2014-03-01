package models.physics

import models.physics.utils._

abstract class Body(
    val density: Float
  , val static: Boolean
) {
  var position: V2
  var velocity: V2
  var acceleration: V2
}

object Body {
}
