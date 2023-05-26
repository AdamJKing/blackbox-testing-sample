import com.typesafe.sbt.packager.docker.*

val scala3Version = "3.3.0"
val http4sVersion = "0.23.18"

lazy val root = project
  .in(file("."))
  .settings(Defaults.itSettings)
  .configs(IntegrationTest)
  .enablePlugins(JavaAppPackaging, DockerPlugin, GitVersioning)
  .settings(
    name := "blackbox-testing-sample",
    scalaVersion := scala3Version,
    scalacOptions += "-release:11",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.typelevel" %% "cats-effect" % "3.5.0",
      "org.typelevel" %% "log4cats-core" % "2.6.0",
      "org.typelevel" %% "log4cats-slf4j" % "2.6.0",
      "ch.qos.logback" % "logback-classic" % "1.4.6" % Runtime,
      "org.typelevel" %% "cats-effect-testing-scalatest" % "1.4.0" % IntegrationTest,
      "org.http4s" %% "http4s-ember-client" % http4sVersion % IntegrationTest,
      "com.dimafeng" %% "testcontainers-scala-scalatest" % "0.40.12" % IntegrationTest
    ),
    (IntegrationTest / test) := (IntegrationTest / test).dependsOn(Docker / publishLocal).value,
    (IntegrationTest / testOnly) := (IntegrationTest / testOnly).dependsOn(Docker / publishLocal).evaluated,
    (IntegrationTest / testQuick) := (IntegrationTest / testQuick).dependsOn(Docker / publishLocal).evaluated,
    javacOptions += s"it.app.dockerfile=${baseDirectory.value / "docker-compose.yaml"}",
    dockerExposedPorts += 8080,
    dockerUpdateLatest := true,
    dockerBaseImage := s"eclipse-temurin:11",
    dockerCommands += Cmd("HEALTHCHECK CMD", "curl localhost:8080/application/status || exit 1")
  )
