package example

import cats.MonadThrow
import cats.effect.Sync
import cats.effect.kernel.Async
import cats.implicits.*
import concurrent.ExecutionContext.Implicits.global
import fs2.io.file.Files
import io.circe.*
import io.circe.syntax.*
import java.io.File
import org.http4s.EntityDecoder
import org.http4s.EntityEncoder
import org.http4s.HttpRoutes
import org.http4s.StaticFile
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits.*
import org.http4s.server.Router
import sttp.tapir.server.http4s.Http4sServerInterpreter

final class Http4sRoutes[F[_]: Async: MonadThrow: Files](
    repository: example.NoteService[F]
):
  val dsl = new Http4sDsl[F] {}
  import dsl.*

  val http4sInterpreter = Http4sServerInterpreter[F]()

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

  val apiTapirRoutes: HttpRoutes[F] =
    http4sInterpreter.toRoutes(
      endpoints.allNotes.serverLogicSuccess(_ =>
        repository
          .getAllNotes()
      )
    ) <+> http4sInterpreter.toRoutes(endpoints.createNote.serverLogicSuccess {
      newNote => repository.createNote(newNote.title, newNote.content)
    })

  val routes = Router(
    "" -> staticRoutes,
    endpoints.notePrefix -> apiTapirRoutes
  )

object Http4sRoutes:
  def make[F[_]: Async: MonadThrow: Files](
      repository: example.NoteService[F]
  ) =
    Sync[F].delay(new Http4sRoutes(repository))
