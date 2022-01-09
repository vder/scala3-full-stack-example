package example

import io.circe.parser
import io.circe.syntax.*
import io.circe.Printer

class JsonParsingSpec extends munit.FunSuite:

  test("parse Note") {
    val note = Note("1234", "Hello, world!", "Nice to meet you")
    val json = note.asJson.noSpaces
    assertEquals(parser.decode[Note](json), Right(note))
  }

  test("parse CreateNote") {
    val createNote = CreateNote("Hello, world!", "Nice to meet you")
    val json = createNote.asJson.noSpaces
    assertEquals(parser.decode[CreateNote](json), Right(createNote))
  }
