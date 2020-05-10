package utils

import java.util.UUID

import akka.Done
import request.LoggedRequest
import play.api.Logging
import play.api.mvc.{Request, RequestHeader, Result}
import play.api.routing.Router.Attrs
import play.api.http.HeaderNames.{REFERER, _}

import scala.concurrent.{ExecutionContext, Future}

object RequestUtils extends Logging {

  val ACTION_NAME_NOT_FOUND = "ACTION_NAME_NOT_FOUND"
  val ORIGIN_NOT_FOUND = "ORIGIN_NOT_FOUND"
  val REFERER_NOT_FOUND = "REFERER_NOT_FOUND"
  val X_FORWARDED_FOR_NOT_FOUND = "X_FORWARDED_FOR_NOT_FOUND"

  val allowedRequestMethods = "HEAD, GET, POST, DELETE, PATCH"
  val allowedRequestHeaders = s"Content-Type, $X_REQUESTED_WITH"

  def getAllowedOrigins(request: Request[_]): String = getRequestOrigin(request).getOrElse("*")

  def getRequestOrigin(request: Request[_]) = {
    request.headers.get(ORIGIN) match {
      case None => request.headers.get(HOST)
      case someOrigin => someOrigin
    }
  }

  def getActionName(request: RequestHeader): String = {
    request.attrs.get(Attrs.HandlerDef) match {
      case Some(handlerDef) =>
        handlerDef.controller.split('.').last + "." + handlerDef.method
      case None => ACTION_NAME_NOT_FOUND
    }
  }

  /**
   * @param request HTTP Request coming to application
   * @return A unique UUID string attached to the request for analysis purposes
   */
  def logRequest[A](request: Request[A])(implicit ec: ExecutionContext): Future[LoggedRequest[A]] = Future {
    val requestId = UUID.randomUUID().toString

    val origin: String = request.headers.get(ORIGIN).getOrElse(request.headers.get(HOST).getOrElse(ORIGIN_NOT_FOUND))
    val referer = request.headers.get(REFERER).getOrElse(REFERER_NOT_FOUND) //Can be used to analyse traffic source
    val xForwardedFor = request.headers.get(X_FORWARDED_FOR).getOrElse(X_FORWARDED_FOR_NOT_FOUND)

    logger.info(s"INCOMING: [action=${getActionName(request)}] [requestId=$requestId] [$ORIGIN=$origin]" +
      s" [$REFERER=$referer] [$X_FORWARDED_FOR=$xForwardedFor]")

    LoggedRequest(requestId, request)
  }

  //TODO: Log the response body also (with caution)
  def logResponse[A](loggedRequest: LoggedRequest[A],
                     response: Result,
                     transactionTimeMillis: Long)
                    (implicit ec: ExecutionContext): Future[Result] = Future {
    logger.info(s"OUTGOING: [action=${getActionName(loggedRequest)}] [requestId=${loggedRequest.requestId}]" +
      s" [status=${response.header.status}] [timeTaken=$transactionTimeMillis]")

    response
  }

  def addAccessControlHeaders(request: Request[_], response: Result): Result = {
    response.withHeaders(
      ACCESS_CONTROL_ALLOW_ORIGIN -> getAllowedOrigins(request),
      ACCESS_CONTROL_ALLOW_METHODS -> allowedRequestMethods,
      ACCESS_CONTROL_ALLOW_HEADERS -> allowedRequestHeaders
    )
  }

}
