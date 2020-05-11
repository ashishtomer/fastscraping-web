package utils

object TimeBasedId {

  private val digitToCharMap = Map[String, String](
    "0" -> "K",
    "1" -> "a",
    "2" -> "l",
    "3" -> "p",
    "4" -> "A",
    "5" -> "n",
    "6" -> "@",
    "7" -> "T",
    "8" -> "o",
    "9" -> "m"
  )

  private val charMapToDigit: Map[String, String] = {
    digitToCharMap.foldLeft(Map.empty[String, String])((result, tuple) => result + (tuple._2 -> tuple._1))
  }

  def get: String = {
    val nanoTime = System.nanoTime()
    val nanoTimeCharArr: Array[String] = nanoTime.toString.split("")
    val id = new StringBuffer(nanoTimeCharArr.length)

    nanoTimeCharArr.foreach(char => id.append(digitToCharMap(char))) //Won't throw exception, as value generated internally

    id.toString
  }

  def getNanoTimestamp(id: String): Long = {
    val idCharArray = id.split("")
    val time = new StringBuffer(idCharArray.length)
    idCharArray.foreach { char: String =>
      val d = charMapToDigit.getOrElse(char, throw new IllegalArgumentException(s"Not valid ID character $char"))
      time.append(d)
    }

    time.toString.toLong
  }

}
