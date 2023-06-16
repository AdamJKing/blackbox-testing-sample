package blackbox.testing.sample

import cats.Eval
import cats.effect.testing.scalatest.{CatsResource, CatsResourceIO}
import cats.effect.{Async, IO, Resource}
import cats.syntax.all.*
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
  def resource[F[_]: Async](uri: Uri): Resource[F, AppClient[F]] =
    EmberClientBuilder.default[F].build.map { http =>
      new AppClient[F] with Http4sClientDsl[F] {
        override def putMessage(msg: String): F[Unit] = http.expect(PUT(msg, uri / "message"))
        override val getMessage: F[String] = http.expect(GET(uri / "message"))
      }
    }

  trait Fixture[F[_]] extends CatsResource[F, AppClient[F]] { self: FixtureAsyncTestSuite =>
    override type FixtureParam = AppClient[F]
    val appUri: Eval[Uri]

    override val resource: Resource[F, AppClient[F]] = AppClient.resource[F](appUri.value)(ResourceAsync)
  }

}
