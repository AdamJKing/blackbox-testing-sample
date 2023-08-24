package blackbox.testing.sample

import cats.effect.Async
import cats.effect.testing.UnsafeRun
import com.dimafeng.testcontainers.{DockerComposeContainer, ServiceLogConsumer, WaitingForService}
import cats.syntax.all.*
import com.dimafeng.testcontainers.scalatest.TestContainersForAll
import org.http4s.Uri
import org.scalatest.concurrent.{IntegrationPatience, PatienceConfiguration}
import org.scalatest.{BeforeAndAfterEach, FixtureAsyncTestSuite}
import org.slf4j.LoggerFactory
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait

import java.io.File

trait AppFixture[F[_]: Async: UnsafeRun]
    extends TestContainersForAll,
      BeforeAndAfterEach,
      AppClient.Fixture[F],
      PatienceConfiguration,
      IntegrationPatience {
  self: FixtureAsyncTestSuite =>

  override type Containers = DockerComposeContainer

  private val dockerComposeYaml = new File(
    scala.sys.props.getOrElse(
      "it.app.dockerfile",
      cancel("this suite requires the property it.app.dockerfile with a path to a valid docker-compose.yaml")
    )
  )

  override lazy val appUri: Uri = withContainers { containers =>
    Uri.unsafeFromString(s"http://${containers.getServiceHost("amdocs", 8080)}:8080")
  }

  override def startContainers(): DockerComposeContainer =
    DockerComposeContainer
      .Def(
        composeFiles = dockerComposeYaml,
        waitingFor = WaitingForService("sample", Wait.forHealthcheck()).some,
        logConsumers = Seq(
          ServiceLogConsumer("sample", new Slf4jLogConsumer(LoggerFactory.getLogger("sample-application")))
        )
      )
      .start()
}
