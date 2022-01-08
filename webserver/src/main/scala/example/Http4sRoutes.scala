package example

import cats.effect.Sync
import cats.implicits.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.HttpRoutes
import org.http4s.StaticFile
import org.http4s.EntityEncoder
import java.io.File
import cats.MonadThrow
import fs2.io.file.Files
import java.nio.file.Path as FSPath
import concurrent.ExecutionContext.Implicits.global
import cats.effect.kernel.Async
import org.http4s.implicits.*
import io.circe.syntax.*
import io.circe.*
import org.http4s.circe.*
import org.http4s.EntityDecoder
import java.nio.file.Paths

final class Http4sRoutes[F[_]: Async: MonadThrow: Files]:
  val dsl = new Http4sDsl[F] {}
  import dsl.*

  val repository = Repository(Paths.get("./target/data/"))

  given EntityDecoder[F, CreateNote] = jsonOf

  private[this] val apiPrefixPath = "/api/"

  val staticRoutes: HttpRoutes[F] =
    HttpRoutes.of[F] {

      case request @ GET -> Root / "assets" / file =>
        StaticFile
          .fromResource(s"assets/$file", Some(request))
          .getOrElseF(NotFound())
      case request @ GET -> Root =>
        StaticFile
          .fromResource("index.html", Some(request))
          .getOrElseF(NotFound())
    }

  val apiRoutes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case request @ GET -> Root / "notes" =>
        Async[F]
          .fromFuture(Async[F].delay(repository.getAllNotes()))
          .flatMap(x => Ok(x.asJson))

      case request @ POST -> Root / "notes" =>
        for
          noteCreated <- request.as[CreateNote]
          note <- Async[F]
            .fromFuture(
              Async[F].delay(
                repository.createNote(noteCreated.title, noteCreated.content)
              )
            )
          resp <- Created(note.asJson)
        yield resp
    }

  val routes = Router(
    "" -> staticRoutes,
    apiPrefixPath -> apiRoutes
  )

object Http4sRoutes:
  def make[F[_]: Async: MonadThrow: Files] =
    Sync[F].delay(new Http4sRoutes())
