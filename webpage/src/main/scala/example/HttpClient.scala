package example

import org.scalajs.dom.*
import scala.scalajs.js
import java.io.IOException
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import io.circe.scalajs.*
import io.circe.syntax.*
import io.circe.parser.decode
import io.circe.Printer
import sttp.client3.UriContext
import cats.syntax.either.*
import sttp.tapir.client.sttp.SttpClientInterpreter
import sttp.client3.impl.cats.FetchCatsBackend
import sttp.client3.FetchBackend

object HttpClient extends NoteService[Future]:

  private val uri = Some(uri"${endpoints.notePrefix}")
  private val backend = FetchBackend()
  private val allNotesCall =
    SttpClientInterpreter().toClientThrowErrors(
      endpoints.allNotes,
      uri,
      backend
    )
  private val createNoteCall =
    SttpClientInterpreter().toClientThrowErrors(
      endpoints.createNote,
      uri,
      backend
    )

  def getAllNotes(): Future[List[Note]] =
    allNotesCall(())

  def createNote(title: String, content: String): Future[Note] =
    createNoteCall(CreateNote(title, content))
