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
import models.physics._

case class Quit()

object Room {

  val world = Akka.system.actorOf(Props[WorldProcessor])
  val viewer = Akka.system.actorOf(Props(new Viewer(world)))
  implicit val timeout = Timeout(1 second)

  val viewTick = Akka.system.scheduler.schedule( 0 milliseconds, 16 milliseconds, viewer, Tick)
  val worldTick = Akka.system.scheduler.schedule( 0 milliseconds, 5 milliseconds, world, Tick)

  def join : scala.concurrent.Future[(Iteratee[JsValue,_],Enumerator[JsValue])] = {
    (viewer ? Join()).map {
      case Connected(enumerator) => {

        println(s"incoming connection")
        val iteratee = Iteratee.foreach[JsValue] { event =>
          //println(event, (event \ "body" \ "shape").as[String])
          viewer ! Command((event \ "body" \ "shape").as[String] match {
            case "box" => (event \ "body").as[BoxBody]
            case "circle" => (event \ "body").as[CircleBody]
          })
        }.map { _ =>
          viewer ! Quit()
        }
        (iteratee,enumerator)
      }
    }
  }
}
