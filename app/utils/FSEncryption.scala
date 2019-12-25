package utils

import java.security.SecureRandom

import com.google.inject.Inject
import org.mindrot.jbcrypt.BCrypt

class FSEncryption @Inject()() {

  def hashPassword(password: String): String = {
    val secureRandom = new SecureRandom()
    val saltSR = Array[Byte](Byte.MaxValue)
    secureRandom.nextBytes(saltSR)

    val salt = BCrypt.gensalt(15, secureRandom)
    BCrypt.hashpw(password, salt)
  }

  def checkPassword(plaintext: String, storedHash: String): Boolean = {
    if (null == storedHash || !storedHash.startsWith("$2a$"))
      throw new java.lang.IllegalArgumentException("Invalid hash provided for comparison")

    BCrypt.checkpw(plaintext, storedHash)
  }

}
