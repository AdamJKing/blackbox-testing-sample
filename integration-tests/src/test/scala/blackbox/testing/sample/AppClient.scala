package blackbox.testing.sample

import cats.effect.testing.UnsafeRun
import cats.effect.{Async, Resource}
import fs2.io.net.Network
import org.http4s.Method.{GET, PUT}
import org.http4s.Uri
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.ember.client.EmberClientBuilder
import org.scalatest.{FixtureAsyncTestSuite, FutureOutcome, Outcome}

trait AppClient[F[_]] {
  def putMessage(msg: String): F[Unit]
  val getMessage: F[String]
}

object AppClient {
  def resource[F[_]: Async: Network](uri: => Uri): Resource[F, AppClient[F]] =
    EmberClientBuilder.default[F].build.map { http =>
      new AppClient[F] with Http4sClientDsl[F] {
        override def putMessage(msg: String): F[Unit] = http.expect(PUT(msg, uri / "message"))
        override val getMessage: F[String] = http.expect(GET(uri / "message"))
      }
    }

  // alternative fixture harness due to issues with the one CE provides
  // https://github.com/typelevel/cats-effect-testing/issues/300
  trait Fixture[F[_]: Async: UnsafeRun: Network] {
    self: FixtureAsyncTestSuite =>

    lazy val appUri: Uri

    private class CEOutcome(outcomeF: F[Outcome]) extends FutureOutcome(UnsafeRun[F].unsafeToFuture(outcomeF))

    override type FixtureParam = AppClient[F]
    override def withFixture(test: OneArgAsyncTest): FutureOutcome = CEOutcome {
      AppClient.resource[F](appUri).use { client =>
        Async[F].async_ { callback =>
          withFixture(test.toNoArgAsyncTest(client))
            .onCompletedThen(result => callback(result.toEither))
        }
      }
    }
  }
}
