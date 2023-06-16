package blackbox.testing.sample

import cats.effect.{IO, IOApp, Ref}
import com.comcast.ip4s.host
import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.dsl.io.*
import org.http4s.server.middleware
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.syntax.*

object Main extends IOApp.Simple {

  given Logger[IO] = Slf4jLogger.getLogger[IO]

  override val run: IO[Unit] = {
    val logging = middleware.Logger.httpRoutes[IO](logHeaders = true, logBody = true)
    info"Starting sample application..." >>
      mkMessageRoute.flatMap { msgRoute =>
        EmberServerBuilder
          .default[IO]
          .withHttpApp(logging(msgRoute).orNotFound)
          .withHost(host"0.0.0.0")
          .build
          .useForever
      }

  }
}
