package example

import scala.concurrent.Future

trait NoteService[F[_]]:
  def getAllNotes(): F[List[Note]]
  def createNote(title: String, content: String): F[Note]
