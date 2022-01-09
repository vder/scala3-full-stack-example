ThisBuild / scalaVersion := "3.1.0"

lazy val tapirVersion = "0.19.3"
lazy val http4sVersion = "0.23.7"
lazy val circeVersion = "0.14.1"
lazy val fs2Version = "3.2.4"

val scalacOpt = Seq(
  "-deprecation",
  "UTF-8",
  "future",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Xfatal-warnings",
  "-Ykind-projector"
)

lazy val webpage = project
  .in(file("webpage"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "scalatags" % "0.11.0",
      "com.softwaremill.sttp.client3" %%% "cats" % "3.3.18",
      "com.softwaremill.sttp.tapir" %%% "tapir-sttp-client" % tapirVersion,
      "org.scala-js" %%% "scalajs-dom" % "2.1.0"
    ),
    scalacOptions ++= scalacOpt
  )
  .dependsOn(core.js)

lazy val webserver = project
  .in(file("webserver"))
  .settings(
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-core" % fs2Version,
      "co.fs2" %% "fs2-io" % fs2Version,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
      "io.circe" %% "circe-fs2" % "0.14.0",
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.scalameta" %% "munit" % "0.7.29" % Test,
      "org.typelevel" %% "cats-effect" % "3.3.3"
    ),
    scalacOptions ++= scalacOpt,
    Compile / resourceGenerators += Def.task {
      val source = (webpage / Compile / scalaJSLinkedFile).value.data
      val dest = (Compile / resourceManaged).value / "assets" / "main.js"
      IO.copy(Seq(source -> dest))
      Seq(dest)
    },
    run / fork := true
  )
  .dependsOn(core.jvm)

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %%% "tapir-core" % tapirVersion,
      "com.softwaremill.sttp.tapir" %%% "tapir-json-circe" % tapirVersion,
      "io.circe" %%% "circe-generic" % circeVersion,
      "io.circe" %%% "circe-parser" % circeVersion,
      "org.scalameta" %%% "munit" % "0.7.29" % Test
    ),
    scalacOptions ++= scalacOpt
  )
