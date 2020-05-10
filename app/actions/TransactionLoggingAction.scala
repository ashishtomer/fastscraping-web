package actions

import play.api.mvc.{Action, BodyParser, Request, Result}
import utils.RequestUtils

import scala.concurrent.{ExecutionContext, Future}

case class TransactionLoggingAction[A](action: Action[A])(implicit val executionContext: ExecutionContext) extends Action[A] {
  override def parser: BodyParser[A] = action.parser

  override def apply(request: Request[A]): Future[Result] = {
    RequestUtils.logRequest(request).flatMap { loggedRequest =>
      val startTime = System.currentTimeMillis()

      action.apply(loggedRequest).flatMap { result =>
        val endTime = System.currentTimeMillis()
        val transactionTime = endTime - startTime

        RequestUtils.logResponse(loggedRequest, result, transactionTime)
      }
    }
  }
}
