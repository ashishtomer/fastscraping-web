package actions

import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

object OpenActionProvider {

  def OpenAction()(implicit cc: ControllerComponents, ec: ExecutionContext) = {
    OpenActionProvider()
  }

  def apply()(implicit cc: ControllerComponents, ec: ExecutionContext): ActionBuilder[Request, AnyContent] = {
    new ActionBuilder[Request, AnyContent] {
      override def parser: BodyParser[AnyContent] = cc.parsers.default

      override protected def executionContext: ExecutionContext = ec

      override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
        block(request)
      }

      override def composeAction[A](action: Action[A]): Action[A] = {
        ResponseHeaderAddingAction[A](
          TransactionLoggingAction[A](
            ExceptionHandlingAction(action)
          )
        )
      }
    }
  }
}
