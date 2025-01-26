lazy val root = (project in file("."))
  .settings(
    name := "auto-config-scala-213",
    ThisBuild / scalaVersion := "2.13.10",
    TaskKey[Unit]("check") := {
      val dependencies = libraryDependencies.value
      val scapegoat =
        dependencies.find(d => d.organization == "com.sksamuel.scapegoat" && d.name == "scalac-scapegoat-plugin")
      if (!scapegoat.map(_.revision).contains("2.1.2")) {
        sys.error(s"Expected Scapegoat version to default to 2.1.2 for scala ${scalaVersion.value}, got: ${scapegoat}")
      }
    },
  )
