package example

import scala.concurrent.ExecutionContext
import munit.CatsEffectSuite
import fs2.io.file.Path
import fs2.io.file.Files
import cats.effect.IO
import cats.implicits.*

class RepositorySpec extends CatsEffectSuite:

  val title = "Hello world!"
  val content = "Nice to meet you."

  test("create and read single note") {
    Files[IO].tempDirectory.use { path =>
      for
        repository <- FileRepository[IO](path)
        note <- repository.createNote(title, content)
        notes <- repository.getAllNotes()
      yield (
        assertEquals(notes.size, 1),
        assertEquals(notes.head, Note(note.id, title, content))
      )
    }
  }

  test("create note twice") {
    Files[IO].tempDirectory.use { path =>
      for
        repository <- FileRepository[IO](path)
        _ <- repository.createNote(title, content)
        _ <- repository.createNote(title, content)
        notes <- repository.getAllNotes()
      yield (
        assertEquals(notes.size, 2),
        assert(notes.forall(n => n.title == title && n.content == content))
      )
    }

  }
