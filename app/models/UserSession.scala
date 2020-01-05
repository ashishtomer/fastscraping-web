package models

import java.sql.Date

case class UserSession(email: String, sessionId: String, startAt: Long, endAt: Option[Long] = None)
