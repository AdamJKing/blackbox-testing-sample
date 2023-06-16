package blackbox.testing.sample

import cats.effect.{IO, Resource}
import cats.syntax.all.*
import cats.effect.testing.scalatest.{AsyncIOSpec, CatsResource, CatsResourceIO}
import com.dimafeng.testcontainers.{ContainerDef, DockerComposeContainer, WaitingForService}
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.scalatest.{FutureOutcome, GivenWhenThen}
import org.scalatest.featurespec.FixtureAsyncFeatureSpec
import org.scalatest.matchers.should.Matchers.*
import org.testcontainers.containers.wait.strategy.{Wait, WaitStrategy}
import org.scalatest.tags.Slow

import java.io.File

@Slow
class AppSpec extends FixtureAsyncFeatureSpec with AsyncIOSpec with AppFixture[IO] with GivenWhenThen {

  Feature("Application") {
    Scenario("Message storage") { (client: AppClient[IO]) =>
      for {
        _ <- IO(Given("the message is unset"))
        _ <- client.getMessage.asserting(_ shouldBe empty)
        _ <- IO(When("the message is set"))
        _ <- client.putMessage("cool!")
        _ <- IO(Then("the message is updated"))
        _ <- client.getMessage.asserting(_ shouldBe "cool!")

      } yield ()
    }
  }
}
