package controllers.actions

import play.api.Logging
import play.api.mvc.{Action, BodyParser, Request, Result}
import utils.RequestUtils

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import utils.ResponseUtils.Messages._

case class ExceptionHandlingAction[A](action: Action[A]) extends Action[A] with Logging {
  override implicit val executionContext: ExecutionContext = action.executionContext
  override val parser: BodyParser[A] = action.parser

  override def apply(request: Request[A]): Future[Result] = {
    try {
      action(request)
    } catch {
      //Add more exceptions here

      case NonFatal(ex: Exception) =>
        logger.error(s"Error 500 while processing request [${request.method} ${request.path}] from " +
          s"[origin=${RequestUtils.getRequestOrigin(request)}]", ex)

        Future(InternalServerError("There is some issue on server side."))

      case err: java.lang.Error =>
        logger.error(s"Unknown error while processing request [${request.method} ${request.path}] from " +
        s"[origin=${RequestUtils.getRequestOrigin(request)}]", err)

        Future(InternalServerError("There is some issue on server side."))

    }
  }
}
