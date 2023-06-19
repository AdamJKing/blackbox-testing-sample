package blackbox.testing.sample

import cats.Eval
import cats.effect.testing.UnsafeRun
import cats.effect.testing.scalatest.{CatsResource, CatsResourceIO}
import cats.effect.{Async, Deferred, IO, Resource}
import cats.syntax.all.*
import fs2.io.net.Network
import org.http4s.Method.{GET, PUT}
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.{Request, Response, Uri}
import org.scalatest.Assertions.fail
import org.scalatest.FixtureAsyncTestSuite

import java.security.{KeyPair, KeyPairGenerator}
import java.time.Instant

trait AppClient[F[_]] {
  def putMessage(msg: String): F[Unit]
  val getMessage: F[String]
}

object AppClient {
  def resource[F[_]: Async: Network](uri: Eval[Uri]): Resource[F, AppClient[F]] =
    EmberClientBuilder.default[F].build.map { http =>
      new AppClient[F] with Http4sClientDsl[F] {
        override def putMessage(msg: String): F[Unit] = http.expect(PUT(msg, uri.value / "message"))
        override val getMessage: F[String] = http.expect(GET(uri.value / "message"))
      }
    }

  /** Locked to [[IO]] for simplicity as `CatsResourceIO` provides an `UnsafeRun[IO]` implementation that isn't
    * available when using `CatsResource[F, _]` directly.
    */
  trait Fixture extends CatsResourceIO[AppClient[IO]] { self: FixtureAsyncTestSuite =>
    override type FixtureParam = AppClient[IO]

    /** Application URI; an `Eval` to allow inheritance and laziness.
      */
    val appUri: Eval[Uri]

    override val resource: Resource[IO, AppClient[IO]] =
      AppClient.resource[IO](appUri)(using ResourceAsync, Network.forAsync(ResourceAsync))
  }

  trait FixtureIO extends CatsResourceIO[AppClient[IO]] { self: FixtureAsyncTestSuite => }
}
