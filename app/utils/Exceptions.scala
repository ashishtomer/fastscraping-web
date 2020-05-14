package utils

object Exceptions {
  private[Exceptions] sealed trait FsException {
    def msg: String
  }

  sealed abstract class FsException1(msg: String) extends IllegalArgumentException(msg) with FsException
  sealed abstract class FsException2(msg: String, t: Throwable) extends IllegalArgumentException(msg, t) with FsException

  case class FsDatabaseError(msg: String) extends FsException1(msg)
  case class FsConfigException(msg: String) extends FsException1(msg)
  case class FsNotFoundException(msg: String) extends FsException1(msg)
  case class SignUpFailedException(msg: String) extends FsException1(msg)
  case class FsUnauthorizedException(msg: String) extends FsException1(msg)
  case class FsSessionNotResetException(msg: String) extends FsException1(msg)

  case class FsSessionNotCreatedException(msg: String, t: Throwable) extends FsException2(msg, t)
}
