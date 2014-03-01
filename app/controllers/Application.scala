package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._

import models._

object Application extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.index("Your new application is ready."))
  }

  def view(username: String) = WebSocket.async[JsValue] { request =>
    Room.join(username)
  }

}
