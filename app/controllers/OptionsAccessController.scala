package controllers

import com.google.inject.Inject
import actions.OpenActionProvider.OpenAction
import play.api.Logging
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.ResponseUtils.Success

import scala.concurrent.{ExecutionContext, Future}

class OptionsAccessController @Inject()(implicit cc: ControllerComponents, ec: ExecutionContext)
  extends AbstractController(cc) with Logging {

  def options(path: String) = OpenAction.async(parse.empty) { _ =>
    Future(Ok)
  }
}
