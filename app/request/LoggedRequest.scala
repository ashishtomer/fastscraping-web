package request

import play.api.Logging
import play.api.mvc.{Request, WrappedRequest}

case class LoggedRequest[A](requestId: String, request: Request[A]) extends WrappedRequest[A](request) with Logging