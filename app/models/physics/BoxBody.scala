package models.physics

import models.physics.utils._
import play.api.libs.json._
import play.api.libs.functional.syntax._

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
  override val shape = "box"
  def topRight = position + size/2
  def topLeft = position + V2(-size.x, size.y)/2
  def bottomLeft = position - size/2
  def bottomRight = position + V2(size.x, -size.y)/2
}

object BoxBody {

  implicit val boxBodyReads:Reads[BoxBody]= (
    (__ \ "position").read[V2] and
    (__ \ "velocity").read[V2] and
    (__ \ "acceleration").read[V2] and
    (__ \ "size").read[V2] and
    (__ \ "density").read[Float] and
    (__ \ "restitution").read[Float] and
    (__ \ "static").read[Boolean] and
    (__ \ "id").read[String]
  )(BoxBody.apply _)

  implicit val boxBodyWrites: Writes[BoxBody] = (
    (__ \ "position").write[V2] and
    (__ \ "velocity").write[V2] and
    (__ \ "acceleration").write[V2] and
    (__ \ "size").write[V2] and
    (__ \ "density").write[Float] and
    (__ \ "restitution").write[Float] and
    (__ \ "static").write[Boolean] and
    (__ \ "id").write[String] and
    (__ \ "shape").write[String]
  )((c:BoxBody) => (
      c.position
    , c.velocity
    , c.acceleration
    , c.size
    , c.density
    , c.restitution
    , c.static
    , c.id
    , c.shape
  ))

}
