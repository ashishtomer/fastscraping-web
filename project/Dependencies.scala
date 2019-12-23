import sbt._

object Dependencies {

  private val slickVersion = "3.3.2"
  private val h2Version = "1.4.199"
  private val playSlickVersion = "4.0.0"
  private val googleGuiceVersion = "4.2.2"
  private val javaMailVersion = "1.4.7"

  private val scalaTest = "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
  private val slick = "com.typesafe.slick" %% "slick" % slickVersion
  private val slickHikariCp = "com.typesafe.slick" %% "slick-hikaricp" % slickVersion
  private val h2Driver = "com.h2database" % "h2" % h2Version //Make it test DB in future
  private val slickForPlay = "com.typesafe.play" %% "play-slick" % playSlickVersion
  private val slickEvolutionForPlay = "com.typesafe.play" %% "play-slick-evolutions" % playSlickVersion
  private val googleGuice = "com.google.inject" % "guice" % googleGuiceVersion
  private val javaMail = "javax.mail" % "mail" % javaMailVersion

  val projectDependencies = Seq(scalaTest, slick, slickHikariCp, h2Driver, slickForPlay, slickEvolutionForPlay,
    googleGuice, javaMail)
}
