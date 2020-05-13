package actions

import play.api.Logging
import play.api.mvc._
import utils.RequestUtils

import scala.concurrent.{ExecutionContext, Future}

case class ResponseHeaderAddingAction[A](action: Action[A]) extends Action[A] with Logging {
  override val parser: BodyParser[A] = action.parser
  override implicit val executionContext: ExecutionContext = action.executionContext

  def apply(request: Request[A]): Future[Result] = {
    action(request).map { result =>
      RequestUtils.addAccessControlHeaders(request, result)
    }
  }

}
