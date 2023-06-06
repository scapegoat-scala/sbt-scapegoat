ThisBuild / scalaVersion := "2.13.10"

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
  val scapegoatModuleA = (moduleA / libraryDependencies).value.find(d => d.organization == "com.sksamuel.scapegoat" && d.name == "scalac-scapegoat-plugin")
  val scapegoatModuleB = (moduleB / libraryDependencies).value.find(d => d.organization == "com.sksamuel.scapegoat" && d.name == "scalac-scapegoat-plugin")
  if (!scapegoatModuleA.map(_.revision).contains("2.1.2")) {
    sys.error(s"Expected Scapegoat version to default to 2.1.2 in module-a for scala ${scalaVersion.value}, got: ${scapegoatModuleA}")
  }
  if (!scapegoatModuleB.map(_.revision).contains("2.1.2")) {
    sys.error(s"Expected Scapegoat version to default to 2.1.2 in module-b for scala ${scalaVersion.value}, got: ${scapegoatModuleB}")
  }
}
