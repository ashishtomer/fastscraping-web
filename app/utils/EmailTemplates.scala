package utils

import play.api.Configuration

object EmailTemplates {

  def confirmSignUp(fullName: String, redirectUrl: String)(implicit configuration: Configuration) = {
    "<div><h1>Hi " + fullName + "</h1><i><a href=\"" + redirectUrl + "\" target="+ "\"_blank\">Please confirm the registration</a></i></div>"
  }

}
