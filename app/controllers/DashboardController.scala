package controllers

import com.google.inject.Inject
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

class DashboardController @Inject()(cc: ControllerComponents)(implicit val ec: ExecutionContext) extends AbstractController(cc) {
  def showUserProfile = {

  }
}
