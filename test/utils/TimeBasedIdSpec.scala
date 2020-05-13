package utils

import org.scalatestplus.play.PlaySpec

class TimeBasedIdSpec extends PlaySpec {

  "TimeBasedId" should {

    "generate correct id with time" in {
      val oneNanoSecond = 1 * 1000 * 1000 * 1000 //1,000,000,000 ns equals to one second
      val timeBeforeId = System.nanoTime()
      val id = TimeBasedId.get
      val creationTimestamp = TimeBasedId.getNanoTimestamp(id)

      assert(timeBeforeId < creationTimestamp)
      assert(creationTimestamp < timeBeforeId + oneNanoSecond * 5) //Considering test won't take longer than 5 seconds
    }

    "get correct number for a valid string" in {
      val testString = "alpAn@TomK"
      val expectedNumber = 1234567890L
      val timestamp = TimeBasedId.getNanoTimestamp(testString)
      assert(timestamp == expectedNumber)
    }
  }
}
