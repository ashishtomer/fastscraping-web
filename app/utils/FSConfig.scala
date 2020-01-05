package utils

import com.google.inject.Inject
import play.api.Configuration

class FSConfig @Inject()(config: Configuration) {
  val loginActiveTime = config.get[Int]("login.active-time")
}
