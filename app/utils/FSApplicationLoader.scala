package utils

import play.api.inject.guice.{GuiceApplicationBuilder, GuiceApplicationLoader, GuiceableModule}
import play.api.{ApplicationLoader, Configuration}

class FSApplicationLoader(guiceApplicationBuilder: GuiceApplicationBuilder) extends GuiceApplicationLoader() {
  def this() = this(new GuiceApplicationBuilder)

  /**
   * Construct a builder to use for loading the given context.
   */
  override def builder(context: ApplicationLoader.Context): GuiceApplicationBuilder = {
    guiceApplicationBuilder
      .disableCircularProxies()
      .in(context.environment)
      .loadConfig(addSysEnvsToConfig(context.initialConfiguration)) //Adding other configuration. Add remote config in future
      .overrides(overrides(context): _*)
  }

  /**
   * Override some bindings using information from the context. The default
   * implementation of this method provides bindings that most applications
   * should include.
   */
  override protected def overrides(context: ApplicationLoader.Context): Seq[GuiceableModule] = {
    GuiceApplicationLoader.defaultOverrides(context)
  }

  private def addSysEnvsToConfig(initialConfig: Configuration): Configuration = {

    val emailHost = System.getenv("FS_EMAIL_HOST")
    val emailPort = System.getenv("FS_EMAIL_PORT")
    val emailSender = System.getenv("FS_EMAIL_SENDER")
    val emailPass = System.getenv("FS_EMAIL_AUTH_PASS")

    val configFromEnvs = Configuration(
      ("email.host" -> emailHost),
      ("email.port" -> emailPort),
      ("email.sender" -> emailSender),
      ("email.auth_pass" -> emailPass)
    )

    initialConfig ++ configFromEnvs
  }
}
