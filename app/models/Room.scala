package models

import akka.actor._
import scala.concurrent.duration._

import play.api._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._

import akka.util.Timeout
import akka.pattern.ask

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

import actors._
import actors.physics._

case class Message(msg: String)
case class Quit()

object Room {

  val world = Akka.system.actorOf(Props[World])
  val viewer = Akka.system.actorOf(Props(new Viewer(world)))
  implicit val timeout = Timeout(1 second)

  val viewTick = Akka.system.scheduler.schedule( 0 milliseconds, 16 milliseconds, viewer, Tick)
  val worldTick = Akka.system.scheduler.schedule( 0 milliseconds, 16 milliseconds, world, Tick)

  def join : scala.concurrent.Future[(Iteratee[JsValue,_],Enumerator[JsValue])] = {
    (viewer ? Join()).map {
      case Connected(enumerator) => {
        println(s"incoming connection")
        val iteratee = Iteratee.foreach[JsValue] { event =>
          viewer ! Message((event \ "msg").as[String])
        }.map { _ =>
          viewer ! Quit()
        }
        (iteratee,enumerator)
      }
    }
  }
}
