lazy val root = (project in file("."))
  .settings(
    name := "test-scapegoat-inclusion-in-pom",
    libraryDependencies += "org.typelevel" %% "cats-core" % "2.12.0",
    ThisBuild / scalaVersion := "2.13.14",
    TaskKey[Unit]("check") := {
      val pom = scala.xml.XML.loadFile(makePom.value)
      (pom \\ "dependencies").map(_ \ "dependency").find(dependency =>
        (dependency \ "groupId").text == "com.sksamuel.scapegoat" &&
          (dependency \ "artifactId").text == "scalac-scapegoat-plugin"
      ).foreach(_ => sys.error("scapegoat compiler plugin is not excluded"))
    }
  )
