package actions

import play.api.Logging
import play.api.mvc.{Action, ActionBuilder, AnyContent, BodyParser, ControllerComponents, Request, Result}

import scala.concurrent.{ExecutionContext, Future}

object OpenActionProvider extends Logging {
  def apply()(implicit cc: ControllerComponents, ec: ExecutionContext): ActionBuilder[Request, AnyContent] = {
    new ActionBuilder[Request, AnyContent] {
      override def parser: BodyParser[AnyContent] = cc.parsers.default
      override protected def executionContext: ExecutionContext = ec

      override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
        block(request)
      }

      override def composeAction[A](action: Action[A]): Action[A] = {
        ExceptionHandlingAction(
          TransactionLoggingAction[A](
            ResponseHeaderAddingAction[A](action)
          )
        )
      }
    }
  }

  def OpenAction()(implicit cc: ControllerComponents, ec: ExecutionContext) = {
    OpenActionProvider()
  }
}
