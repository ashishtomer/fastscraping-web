package models

import java.sql.Date

case class UserSession(email: String, sessionId: String, startAt: Date, endAt: Option[Date] = None)
