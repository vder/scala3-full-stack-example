package example

import scala.concurrent.ExecutionContext
import org.scalajs.dom
import dom.html
import scalajs.js.annotation.JSExport
import scalatags.JsDom.all.*
import org.scalajs.dom.*

object WebPage:
  given ExecutionContext = ExecutionContext.global

  val titleInput = input(`type` := "text").render
  val contentTextArea = textarea.render
  val saveButton = button("Create Note").render

  val cookieValue = document.cookie
    .split(";")
    .find(_.startsWith("sessionId="))
    .fold("BRAK")(cookie => cookie.substring(10))

  val statusDiv = div(cls := "status-div", h2(cookieValue))

  val form = div(
    cls := "note-form",
    titleInput,
    contentTextArea,
    saveButton
  )

  val appContainer = div(
    id := "app-container",
    h1("My Notepad"),
    form
  ).render

  def addNote(note: Note): Unit =
    val elem = div(
      cls := "note",
      h2(note.title),
      p(note.content)
    ).render
    appContainer.appendChild(elem)

  saveButton.onclick = (e: dom.Event) =>
    HttpClient
      .createNote(titleInput.value, contentTextArea.value)
      .map(addNote)

  @main def start: Unit =
    document.body.appendChild(appContainer)
    document.body.appendChild(statusDiv.render)

  for notes <- HttpClient.getAllNotes(); note <- notes do addNote(note)
