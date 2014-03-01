package actors

import akka.actor._
import scala.concurrent.duration._
import scala.language.postfixOps

import play.api._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._

import akka.util.Timeout
import akka.pattern.ask

import actors.physics._

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._

import models.physics._
import models.physics.utils._

case class Join()
case class Command(body: Body)
case class Connected(enumerator:Enumerator[JsValue])
case class CannotConnect(msg: String)

class Viewer(world: ActorRef) extends Actor {
  var members = Set.empty[String]
  val (enum, channel) = Concurrent.broadcast[JsValue]
  implicit val defaultTimeout = Timeout(1 second)

  def receive = {
    case j:Join => {
      sender ! Connected(enum)
    }
    case Tick => {
      (world ? GetBodies).mapTo[Map[String, Body]].map { m =>
        send(Json.toJson(m))
      }
    }
    case m:Command => {
      world ! NewBody(m.body)
    }
  }
  def send(msg: String) {
    send(JsObject(Seq("msg" -> JsString(msg))))
  }
  def send(msg: JsValue) {
    channel.push(msg)
  }

}

