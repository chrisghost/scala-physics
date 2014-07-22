package utilities

import play.api.{ Logger => PlayLogger }
import ch.qos.logback.classic.Level

trait Logger {
  protected[this] val logcontext = (getClass.getCanonicalName).replace("$", "")
  protected[this] lazy val LOGGER = PlayLogger(logcontext)
}
