package models


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

import actors._

case class Message(username: String, msg: String)
case class Quit(username: String)

object Room {

  val viewer = Akka.system.actorOf(Props[Viewer])
  implicit val timeout = Timeout(1 second)

  val cancellable =
    Akka.system.scheduler.schedule(
      0 milliseconds,
      500 milliseconds,
      viewer,
      Tick)

  def join(username:String):scala.concurrent.Future[(Iteratee[JsValue,_],Enumerator[JsValue])] = {
    (viewer ? Join(username)).map {
      case Connected(enumerator) =>
        println(s"incoming connection from ${username}")
        // Create an Iteratee to consume the feed
        val iteratee = Iteratee.foreach[JsValue] { event =>
          viewer ! Message(username, (event \ "msg").as[String])
        }.map { _ =>
          viewer ! Quit(username)
        }
        (iteratee,enumerator)
      case CannotConnect(error) =>
        // Connection error
        // A finished Iteratee sending EOF
        val iteratee = Done[JsValue,Unit]((),Input.EOF)
        // Send an error and close the socket
        val enumerator = Enumerator[JsValue](JsObject(Seq("error" -> JsString(error)))).andThen(Enumerator.enumInput(Input.EOF))
        (iteratee,enumerator)
    }
  }
}
