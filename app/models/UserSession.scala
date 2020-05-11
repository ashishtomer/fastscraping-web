package models

import utils.Exceptions.FsConfigException

case class UserSession(email: String, sessionId: String, startAt: Long, endAt: Option[Long] = None) {
  //End time can't be smaller than or equal to start time
  require(endAt.getOrElse(Long.MaxValue) > startAt)

  def isExpired(expireDuration: Option[Long]): Boolean = {
    val currentMillis = System.currentTimeMillis()

    (endAt.isDefined && endAt.get <= currentMillis) ||
    ((currentMillis - startAt) > expireDuration.getOrElse(throw FsConfigException("Expire duration is not defined")))
  }

  def encrypt: Unit = {

  }

  def decrypt: Unit = {

  }
}
