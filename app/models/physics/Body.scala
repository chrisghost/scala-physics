package models.physics

import models.physics.utils._

import play.api.libs.json._
import play.api.libs.functional.syntax._

abstract class Body(
    val density: Float
  , val static: Boolean
  , val id: String = ""
) {
  var position: V2
  var velocity: V2
  var acceleration: V2
  def mass: Float = ???
}

object Body {

  implicit val bodyWriter = Writes[Body] { b =>
    b match {
      case box: BoxBody => Json.toJson(box)
    }
  }
}
