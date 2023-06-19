import com.typesafe.sbt.packager.docker.*

val http4sVersion = "0.23.20"

lazy val server = project
  .enablePlugins(JavaAppPackaging, DockerPlugin, GitVersioning)
  .settings(
    name := "blackbox-testing-sample",
    ThisBuild / scalaVersion := "3.3.0",
    scalacOptions ++= Seq("-release:11", "-no-indent", "-source:future", "-deprecation"),
    libraryDependencies ++=
      Seq(
        "org.http4s" %% "http4s-dsl" % http4sVersion,
        "org.http4s" %% "http4s-ember-server" % http4sVersion,
        "org.typelevel" %% "log4cats-core" % "2.6.0",
        "org.typelevel" %% "log4cats-slf4j" % "2.6.0",
        "ch.qos.logback" % "logback-classic" % "1.4.7" % Runtime
      ),
    dockerExposedPorts += 8080,
    dockerUpdateLatest := true,
    dockerBaseImage := s"eclipse-temurin:11",
    dockerCommands += Cmd("HEALTHCHECK CMD", "curl localhost:8080/application/status || exit 1")
  )

lazy val `integration-tests` = project
  .settings(
    publish / skip := true,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect-testing-scalatest" % "1.4.0",
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "com.dimafeng" %% "testcontainers-scala-scalatest" % "0.40.12",
      "ch.qos.logback" % "logback-classic" % "1.4.7" % Runtime
    ),
    Test / javaOptions += s"-Dit.app.dockerfile=${(ThisBuild / baseDirectory).value / "docker-compose.yaml"}",
    Test / fork := true
  )
