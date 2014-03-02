import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

import play.libs.Akka
import play.api.libs.concurrent._
import scala.concurrent.ExecutionContext
import scala.concurrent._
import scala.concurrent.duration._

import akka.actor._
import akka.pattern.ask

import scala.util._

import actors.physics._

import models.physics._
import models.physics.utils._
import reflect.ClassTag

class WorldSpec extends Specification {

  val box = BoxBody( V2(0, 0), V2(1, 1), V2(1, 1), V2(1.0f, 1.0f), 1.0f, 1.0f, false, "d1")
  val bigBox = BoxBody( V2(100, 0), V2(1, 1), V2(1, 1), V2(10.0f, 10.0f), 1.0f, 1.0f, false, "d2")

  "World" should {
    "create bodies" in running(FakeApplication()) {
      val world = Akka.system.actorOf(Props[World])

      world ! NewBody(box)

      val lst = Await.result((world ? GetBodies).mapTo[Map[String, Body]], Duration.Inf)

      lst.size === 1
    }
    "update dynamic bodies positions" in running(FakeApplication()) {
      implicit val ec: ExecutionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext
      val world = Akka.system.actorOf(Props[World])

      world ! NewBody(box.copy(velocity = V2(100, 100)))
      world ! NewBody(box.copy(id="ds", static = true))
      world ! FakeTick(10)

      val lst = Await.result((world ? GetBodies).mapTo[Map[String, Body]], Duration.Inf)

      lst("d1").position.x !== box.position.x
      lst("d1").position.y !== box.position.y

      lst("ds").position.x mustEqual(box.position.x)
      lst("ds").position.y mustEqual(box.position.y)
    }
    "fall down because of gravity" in running(FakeApplication()) {
      implicit val ec: ExecutionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext
      val world = Akka.system.actorOf(Props[World])

      world ! NewBody(box.copy(velocity = V2(0, 0), acceleration= V2(0, 0)))
      world ! NewBody(bigBox.copy(velocity = V2(0, 0), acceleration= V2(0, 0)))
      world ! FakeTick(10)//first time to init acceleration
      world ! FakeTick(10)

      val lst = Await.result((world ? GetBodies).mapTo[Map[String, Body]], Duration.Inf)

      lst("d1").position.y must beLessThan(box.position.y)
      lst("d2").position.y must beLessThan(bigBox.position.y)

      lst("d2").position.y must beLessThan(lst("d1").position.y) // d2 is bigger (so heavier) than d1
    }
  }
}
