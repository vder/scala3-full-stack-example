package example

import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import io.circe.Json

object endpoints:

  val notePrefix = "api/notes"
  val allNotes = endpoint.get.out(jsonBody[List[Note]])
  val createNote = endpoint.post
    .in(jsonBody[CreateNote])
    .out(jsonBody[Note])
