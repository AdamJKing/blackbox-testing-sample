package blackbox.testing.sample

import cats.effect.testing.scalatest.{AsyncIOSpec, CatsResource, CatsResourceIO}
import cats.effect.IO
import cats.syntax.all.*
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import org.scalatest.featurespec.FixtureAsyncFeatureSpec
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.tags.Slow
import org.scalatest.GivenWhenThen

import scala.concurrent.Future
import org.scalatest.concurrent.Eventually
import org.scalatest.concurrent.IntegrationPatience
import org.scalatest.concurrent.PatienceConfiguration
import scala.concurrent.ExecutionContext
import cats.effect.unsafe.IORuntimeBuilder

@Slow
class AppSpec extends FixtureAsyncFeatureSpec, AsyncIOSpec, AppFixture[IO], GivenWhenThen {
  Feature("Application") {
    Scenario("Message storage") { client =>
      for {
        _ <- IO(Given("the message is unset"))
        _ <- client.getMessage.asserting(_.shouldBe(empty))
        _ <- IO(When("the message is set"))
        _ <- client.putMessage("cool!")
        _ <- IO(Then("the message is updated"))
        _ <- client.getMessage.asserting(_.shouldBe("cool!"))

      } yield ()
    }
  }
}
