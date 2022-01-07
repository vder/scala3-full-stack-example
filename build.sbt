ThisBuild / scalaVersion := "3.1.0"

lazy val webpage = project
  .in(file("webpage"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "2.1.0"
    )
  )
  .dependsOn(core.js)

lazy val webserver = project
  .in(file("webserver"))
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.2.6" cross CrossVersion.for3Use2_13,
      "com.typesafe.akka" %% "akka-stream" % "2.6.16" cross CrossVersion.for3Use2_13,
      "org.scalameta" %% "munit" % "0.7.29" % Test,
      "org.http4s" %% "http4s-blaze-server" % "0.23.7",
      "org.http4s" %% "http4s-dsl" % "0.23.7",
      "org.http4s" %% "http4s-circe" % "0.23.7"
    ),
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
      "io.circe" %%% "circe-generic" % "0.14.1",
      "io.circe" %%% "circe-parser" % "0.14.1",
      "org.scalameta" %%% "munit" % "0.7.29" % Test
    )
  )
