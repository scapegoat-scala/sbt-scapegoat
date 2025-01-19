enablePlugins(ScapegoatSbtPlugin)

lazy val root = project
  .in(file("."))
  .settings(
    name := "scapegoat-example",
    scalaVersion := "2.13.10",
    crossScalaVersions := Seq(
      "2.11.12",
      "2.12.17",
      "2.13.10",
    ),
    scapegoatVersion := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 11)) => "1.4.9"
        case _ => "2.1.0"
      }
    },
    TaskKey[Unit]("check") := {
      scalaVersion.value match {
        case "2.11.12" =>
          if (scapegoatVersion.value != "1.4.9") {
            sys.error(
              s"Expected Scapegoat version to default to 1.4.9 for scala ${scalaVersion.value}, got: ${scapegoatVersion.value}",
            )
          }
        case "2.12.17" | "2.13.10" =>
          if (scapegoatVersion.value != "2.1.0") {
            sys.error(
              s"Expected Scapegoat version to default to 2.1.0 for scala ${scalaVersion.value}, got: ${scapegoatVersion.value}",
            )
          }
      }
    },
  )
