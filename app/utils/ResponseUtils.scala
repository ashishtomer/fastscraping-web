package utils

import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.{Format, JsValue, Json}
import play.api.mvc.Result
import play.api.mvc.Results.Status

object ResponseUtils {

  case class SuccessMsg(message: String) {
    override def toString = Json.prettyPrint(this.json)

    def json = Json.toJson(this)
  }

  object SuccessMsg {
    implicit val format: Format[SuccessMsg] = Json.format[SuccessMsg]
  }

  case class ErrorMsg(error: String, cause: String) {
    override def toString = Json.prettyPrint(this.json)

    def json = Json.toJson(this)
  }

  object ErrorMsg {
    implicit val format: Format[ErrorMsg] = Json.format[ErrorMsg]
  }

  object Error {
    def apply(error: String): JsValue = ErrorMsg(error, error).json
    def apply(error: String, cause: String): JsValue = ErrorMsg(error, cause).json
  }

  object Success {
    def apply(message: String): JsValue = SuccessMsg(message).json
  }
}
