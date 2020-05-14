package helpers

import java.io.File

import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import play.api.ApplicationLoader.Context
import play.api.mvc.ControllerComponents
import play.api.test.{Helpers, StubControllerComponentsFactory}
import play.api.{Application, Configuration, Environment, Play}
import utils.FSApplicationLoader

import scala.concurrent.ExecutionContext

trait FsTestBed extends PlaySpec with StubControllerComponentsFactory with MockedClasses with BeforeAndAfterAll {
  //Configuration
  implicit lazy val testConfiguration: Configuration =
    Configuration(ConfigFactory.parseFile(new File("conf/application-test.conf")))

  implicit lazy val testApp: Application = {
    new FSApplicationLoader()
      .builder(Context.create(Environment.simple()))
      .configure(testConfiguration)
      .build()
  }

  implicit lazy val ec: ExecutionContext = testApp.injector.instanceOf[ExecutionContext]
  implicit lazy val stubCC: ControllerComponents = Helpers.stubControllerComponents()

  override def beforeAll(): Unit = {
    Play.start(testApp)
  }
}
