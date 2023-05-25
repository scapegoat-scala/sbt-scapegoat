ThisBuild / scalaVersion := "2.13.10"
ThisBuild / scapegoatVersion := "2.1.0"

lazy val moduleA = (project in file("module-a"))
  .settings(
    name := "module-a",
  )

lazy val moduleB = (project in file("module-b"))
  .dependsOn(moduleA)
  .settings(
    name := "module-b",
  )

lazy val root = (project in file("."))
  .aggregate(moduleA, moduleB)
  .settings(
    name := "root",
  )

TaskKey[Unit]("check") := {
  if ((moduleA / scapegoatVersion).value != "2.1.0") {
    sys.error(s"Expected Scapegoat version to default to 2.1.0 in module-a for scala ${scalaVersion.value}, got: ${scapegoatVersion.value}")
  }
  if ((moduleB / scapegoatVersion).value != "2.1.0") {
    sys.error(s"Expected Scapegoat version to default to 2.1.0 in module-b for scala ${scalaVersion.value}, got: ${scapegoatVersion.value}")
  }
}
