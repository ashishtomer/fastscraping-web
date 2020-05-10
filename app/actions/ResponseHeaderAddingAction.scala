package actions

import play.api.Logging
import play.api.mvc.{Action, BodyParser, Request, Result}
import request.LoggedRequest
import utils.RequestUtils
import utils.ResponseUtils.Messages._

import scala.concurrent.{ExecutionContext, Future}

case class ResponseHeaderAddingAction[A](action: Action[A]) extends Action[A] with Logging {
  override val parser: BodyParser[A] = action.parser
  override implicit val executionContext: ExecutionContext = action.executionContext

  def apply(request: Request[A]): Future[Result] = {
    request match {
      case req: LoggedRequest[A] =>
        action(request).map { result =>
          RequestUtils.addAccessControlHeaders(req, result)
        }
      case _ =>
        logger.error("Request is not of type LoggedRequest")
        Future(InternalServerError("There is some issue on server side."))
    }
  }
}
