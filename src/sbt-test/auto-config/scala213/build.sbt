lazy val root = (project in file("."))
  .settings(
    name := "auto-config-scala-213",
    ThisBuild / scalaVersion := "2.13.17",
    TaskKey[Unit]("check") := {
      val dependencies = libraryDependencies.value
      val scapegoat =
        dependencies.find(d => d.organization == "com.sksamuel.scapegoat" && d.name == "scalac-scapegoat-plugin")
      if (!scapegoat.map(_.revision).contains("3.2.0")) {
        sys.error(s"Expected Scapegoat version to default to 3.2.0 for scala ${scalaVersion.value}, got: ${scapegoat}")
      }
    },
  )
