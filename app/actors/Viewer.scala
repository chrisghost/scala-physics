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

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

case class Join(username: String)
case class Connected(enumerator:Enumerator[JsValue])
case class CannotConnect(msg: String)
case class Tick()

class Viewer extends Actor {
  var members = Set.empty[String]
  val (enum, channel) = Concurrent.broadcast[JsValue]

  def receive = {
    case j:Join => {
      if(members.contains(j.username)) {
        sender ! CannotConnect("This username is already used")
      } else {
        members = members + j.username
        send("hello")
        sender ! Connected(enum)
      }
    }
    case Tick => {
      send("tickB")
    }
  }
  def send(msg: String) {
    send(JsObject(Seq("msg" -> JsString(msg))))
  }
  def send(msg: JsValue) {
    channel.push(msg)
  }

}

