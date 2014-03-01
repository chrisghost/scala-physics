import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

import models.physics.utils._

class V2Spec extends Specification {

  "V2" should {
    val a = V2(1, 1)
    val b = V2(7, 2)
    "be addable" in {
      a+b === V2(8, 3)
    }
    "be substracable" in {
      a-b === V2(-6, -1)
    }
    "be multiplicate by a scalar" in {
      b*2 === V2(14, 4)
    }
  }
}
