package models.physics

import models.physics.utils._
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class CircleBody(
    var position: V2
  , var velocity: V2
  , var acceleration: V2
  , radius: Double
  , override val density: Float
  , override val restitution: Float
  , override val static: Boolean
  , override val id: String = ""
) extends Body(density, restitution, static, id) {

  override def mass = math.Pi * math.pow(radius, 2) * density
  override val invMass = 1/mass
  override val shape = "circle"
}

object CircleBody {

  implicit val circleBodyReads:Reads[CircleBody]= (
    (__ \ "position").read[V2] and
    (__ \ "velocity").read[V2] and
    (__ \ "acceleration").read[V2] and
    (__ \ "radius").read[Double] and
    (__ \ "density").read[Float] and
    (__ \ "restitution").read[Float] and
    (__ \ "static").read[Boolean] and
    (__ \ "id").read[String]
  )(CircleBody.apply _)

  implicit val circleBodyWrites: Writes[CircleBody] = (
    (__ \ "position").write[V2] and
    (__ \ "velocity").write[V2] and
    (__ \ "acceleration").write[V2] and
    (__ \ "radius").write[Double] and
    (__ \ "density").write[Float] and
    (__ \ "restitution").write[Float] and
    (__ \ "static").write[Boolean] and
    (__ \ "id").write[String] and
    (__ \ "shape").write[String]
  )((c:CircleBody) => (
      c.position
    , c.velocity
    , c.acceleration
    , c.radius
    , c.density
    , c.restitution
    , c.static
    , c.id
    , c.shape
  ))
}
