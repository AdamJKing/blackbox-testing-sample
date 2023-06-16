import com.typesafe.sbt.packager.docker.*

val http4sVersion = "0.23.20"

lazy val root = project
  .enablePlugins(JavaAppPackaging, DockerPlugin, GitVersioning)
  .settings(
    name := "blackbox-testing-sample",
    ThisBuild / scalaVersion := "3.3.0",
    scalacOptions ++= Seq("-release:11", "-no-indent", "-source:future"),
    libraryDependencies ++=
      Seq(
        "org.http4s" %% "http4s-dsl" % http4sVersion,
        "org.http4s" %% "http4s-ember-server" % http4sVersion,
        "org.typelevel" %% "log4cats-core" % "2.6.0",
        "org.typelevel" %% "log4cats-slf4j" % "2.6.0",
        "ch.qos.logback" % "logback-classic" % "1.4.7" % Runtime,
        "org.typelevel" %% "cats-effect-testing-scalatest" % "1.4.0" % Test,
        "org.http4s" %% "http4s-ember-client" % http4sVersion % Test,
        "com.dimafeng" %% "testcontainers-scala-scalatest" % "0.40.12" % Test
      ),
    javacOptions += s"it.app.dockerfile=${baseDirectory.value / "docker-compose.yaml"}",
    dockerExposedPorts += 8080,
    dockerUpdateLatest := true,
    dockerBaseImage := s"eclipse-temurin:11",
    dockerCommands += Cmd("HEALTHCHECK CMD", "curl localhost:8080/application/status || exit 1")
  )
