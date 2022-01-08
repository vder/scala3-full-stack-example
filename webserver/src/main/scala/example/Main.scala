package example

import cats.effect.IOApp
import cats.effect.IO
import org.http4s.blaze.server.BlazeServerBuilder
import scala.concurrent.ExecutionContext.global
import org.http4s.implicits.*
import org.http4s.server.staticcontent.*
import cats.implicits.*
import fs2.io.file.Path

object Main extends IOApp.Simple:
  def run =
    for
      repo <- FileRepository[IO](Path("./target/data/"))
      routes <- Http4sRoutes.make[IO](repo)
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
