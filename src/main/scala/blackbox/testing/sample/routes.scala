package blackbox.testing.sample

import cats.Monad
import cats.syntax.all.*
import cats.effect.Concurrent
import cats.effect.{IO, Ref}
import org.http4s.HttpRoutes
import org.http4s.Method.GET
import org.http4s.dsl.io.*

val mkMessageRoute: IO[HttpRoutes[IO]] = {
  def build(msg: Ref[IO, String]): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "application" / "status" => Ok("okay!")

    case GET -> Root / "message" => Ok(msg.get)

    case req @ PUT -> Root / "message" =>
      req.as[String].flatTap(a => msg.set(a)) >> NoContent()
  }

  Ref[IO].of("").map(build)
}
