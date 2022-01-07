package example

import cats.effect.IOApp
import cats.effect.IO
import org.http4s.blaze.server.BlazeServerBuilder
import scala.concurrent.ExecutionContext.global
import org.http4s.implicits.*
import org.http4s.server.staticcontent.*
import cats.implicits.*

object Main extends IOApp.Simple:
  def run =
    for
      routes <- Http4sRoutes.make[IO]
      _ <- BlazeServerBuilder[IO]
        .withExecutionContext(global)
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(
          routes.routes.orNotFound
        )
        .serve
        .compile
        .drain
    yield ()
