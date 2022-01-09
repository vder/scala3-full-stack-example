package example

import java.util.UUID
import cats.implicits.*
import cats.syntax.either.*
import io.circe.syntax.*
import io.circe.parser.decode
import io.circe.Printer
import fs2.io.file.Files
import fs2.io.file.Path
import cats.Monad
import cats.MonadThrow
import fs2.text
import io.circe.fs2.*
import cats.effect.kernel.Sync

final class FileRepository[F[_]: Files: Sync: MonadThrow] private (
    directory: Path
) extends NoteService[F]:
  def getAllNotes(): F[List[Note]] =
    Files[F]
      .walk(directory)
      .filter(_.toString.endsWith(".json"))
      .flatMap { file =>
        Files[F]
          .readAll(file)
          .through(text.utf8.decode)
          .through(stringStreamParser[F])
          .through(decoder[F, Note])
      }
      .compile
      .toList

  def createNote(title: String, content: String): F[Note] =
    val id = UUID.randomUUID().toString
    val note = Note(id, title, content)
    val file = directory / s"$id.json"

    fs2.Stream
      .emit(note.asJson.noSpaces)
      .covary[F]
      .through(text.utf8.encode)
      .through(Files[F].writeAll(file))
      .compile
      .drain
      .as(note)

object FileRepository:

  def apply[F[_]: Files: MonadThrow: Sync](directory: Path): F[NoteService[F]] =
    Files[F]
      .createDirectory(directory)
      .attempt
      .map(_ => new FileRepository(directory))
