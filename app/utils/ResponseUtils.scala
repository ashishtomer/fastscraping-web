package utils

import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.{Format, Json}
import play.api.mvc.Result
import play.api.mvc.Results.Status

object ResponseUtils {

  case class Success(message: String) {
    override def toString = Json.prettyPrint(this.json)

    def json = Json.toJson(this)
  }

  object Success {
    implicit val format: Format[Success] = Json.format[Success]

    def apply(any: Any): Success = new Success(any.toString)
  }

  case class Error(error: String, cause: String) {
    override def toString = Json.prettyPrint(this.json)

    def json = Json.toJson(this)
  }

  object Error {
    implicit val format: Format[Error] = Json.format[Error]

    def apply(error: String): Error = new Error(error, error)

    def apply(any: Any): Error = Error.apply(any.toString)
  }

  /********** Quick responses **********/
  object Messages {
    val InternalServerError: String => Result = content => Status(INTERNAL_SERVER_ERROR)(content)
  }

}
