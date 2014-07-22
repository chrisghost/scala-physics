package actors

import akka.actor._
import scala.concurrent.duration._
import scala.language.postfixOps

import play.api._
import play.api.libs.json._
import play.api.libs.functional.syntax._
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
import utilities.Logger

case class Join()

abstract class Command(kind: String)
object Command extends Logger {
  implicit val commandFormat = new Format[Command] {
    def reads(js: JsValue): JsResult[Command] = {
      val k = (js \ "kind").as[String]
      k match {
        case CreateBody.KIND => CreateBody.reader.reads(js)
        case CleanBodies.KIND => CleanBodies.reads(js)
        case DeleteBody.KIND => DeleteBody.reader.reads(js)
        case _ => JsError()
      }

    }
    def writes(c: Command) = ???
  }
}

case class CreateBody(kind: String, body: Body) extends Command("body")
object CreateBody {
  val KIND = "create"
  def apply(body: Body): CreateBody = CreateBody(KIND, body)
  implicit val reader:Reads[CreateBody]= (
    (__ \ "kind").read[String] and
    (__ \ "body").read[Body]
  )((_, body: Body) => {
    CreateBody(body)
  })
}

case class DeleteBody(kind: String, id: String) extends Command("delete")
object DeleteBody {
  val KIND = "delete"
  def apply(id: String): DeleteBody = DeleteBody(KIND, id)
  implicit val reader:Reads[DeleteBody]= (
    (__ \ "kind").read[String] and
    (__ \ "id").read[String]
  )((_, id: String) => {
    DeleteBody(KIND, id)
  })
  implicit val writer:Writes[DeleteBody] = (
    (__ \ "kind").write[String] and
    (__ \ "id").write[String]
  )((db: DeleteBody) => (db.kind, db.id))
}

case class CleanBodies(kind: String) extends Command("clean")
object CleanBodies {
  val KIND = "clean"
  def apply: CleanBodies = CleanBodies(KIND)
  def reads(js:JsValue): JsResult[CleanBodies] = JsSuccess(CleanBodies(KIND))
}

case class Connected(enumerator:Enumerator[JsValue])
case class CannotConnect(msg: String)

class Viewer(world: ActorRef) extends Actor with Logger {
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
    case cb:CreateBody => {
      world ! NewBody(cb.body)
    }
    case cb:CleanBodies => {
      (world ? CleanActiveBodies).map { e =>
        e match {
          case lst: List[String] => lst.map { id => send(Json.toJson(DeleteBody(id))(DeleteBody.writer)) }
          case _ => LOGGER.error("CleanBodies did not return a List[String], um that's bad m'kay?")
        }
      }
    }
    case _ => {
      LOGGER.error("got some undefined command")
    }
  }
  def send(msg: String) {
    send(JsObject(Seq("msg" -> JsString(msg))))
  }
  def send(msg: JsValue) {
    channel.push(msg)
  }

}

