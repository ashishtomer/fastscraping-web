package actions

import play.api.Logging
import play.api.mvc.Results._
import play.api.mvc.{Action, BodyParser, Request, Result}
import utils.Exceptions.{FsNotFoundException, FsUnauthorizedException}
import utils.RequestUtils
import utils.ResponseUtils.Error

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

case class ExceptionHandlingAction[A](action: Action[A]) extends Action[A] with Logging {
  override implicit val executionContext: ExecutionContext = action.executionContext
  override val parser: BodyParser[A] = action.parser

  override def apply(request: Request[A]): Future[Result] = {
    action(request) recover {
      case NonFatal(ex: FsNotFoundException) =>
        logger.error(s"Error processing request", ex)
        NotFound(Error(ex.getMessage))

      case NonFatal(ex: FsUnauthorizedException) =>
        logger.error(s"Error processing request", ex)
        BadRequest(Error(ex.getMessage))

      case NonFatal(ex: Exception) =>
        logger.error(s"Error 500 while processing request [${request.method} ${request.path}] from " +
          s"[origin=${RequestUtils.getRequestOrigin(request)}]", ex)
        InternalServerError(Error("There is some issue on server side.", ex.getMessage))

      case err: java.lang.Error =>
        logger.error(s"Unknown error while processing request [${request.method} ${request.path}] from " +
          s"[origin=${RequestUtils.getRequestOrigin(request)}]", err)
        InternalServerError(Error("There is some issue on server side."))
    }
  }
}
