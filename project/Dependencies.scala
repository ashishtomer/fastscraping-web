import play.sbt.PlayImport
import sbt._

object Dependencies {

  private val slickVersion = "3.3.2"
  private val h2Version = "1.4.199"
  private val playSlickVersion = "4.0.0"
  private val javaMailVersion = "1.4.7"

  private val scalaTest = "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
  private val mockito =  "org.mockito" % "mockito-all" % "1.10.19" % Test
  private val slick = "com.typesafe.slick" %% "slick" % slickVersion
  private val slickHikariCp = "com.typesafe.slick" %% "slick-hikaricp" % slickVersion
  private val h2Driver = "com.h2database" % "h2" % h2Version //Make it test DB in future
  private val slickForPlay = "com.typesafe.play" %% "play-slick" % playSlickVersion
  private val slickEvolutionForPlay = "com.typesafe.play" %% "play-slick-evolutions" % playSlickVersion
  private val javaMail = "javax.mail" % "mail" % javaMailVersion
  private val pgDriver = "org.postgresql" % "postgresql" % "42.2.9"
  private val jBcrypt = "org.mindrot" % "jbcrypt" % "0.4"

  val projectDependencies = Seq(scalaTest, slick, slickHikariCp, h2Driver, slickForPlay, slickEvolutionForPlay,
    PlayImport.guice, javaMail, pgDriver, jBcrypt, mockito)
}
