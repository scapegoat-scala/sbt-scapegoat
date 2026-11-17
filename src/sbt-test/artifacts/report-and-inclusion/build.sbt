import sbtcompat.PluginCompat._

lazy val root = (project in file("."))
  .settings(
    name := "test-scapegoat-inclusion-in-pom",
    libraryDependencies += "org.typelevel" %% "cats-core" % "2.12.0",
    ThisBuild / scalaVersion := "2.13.14",
    TaskKey[Unit]("checkFailureOutputs") := Def.uncached {
      val report = file(scapegoatOutputPath.value) / "scapegoat.xml"
      val classFile = (Compile / classDirectory).value / "Boot.class"
      assert(report.isFile(), s"Missing Scapegoat report: $report")
      assert(!classFile.exists(), s"Class generated despite failed analysis: $classFile")
    },
    TaskKey[Unit]("checkNoReport") := Def.uncached {
      val report = file(scapegoatOutputPath.value) / "scapegoat.xml"
      assert(!report.exists(), s"Normal compilation generated a Scapegoat report: $report")
    },
    TaskKey[Unit]("check") := Def.uncached {
      implicit val converter: xsbti.FileConverter = fileConverter.value
      val pom = scala.xml.XML.loadFile(sbtcompat.PluginCompat.toFile(makePom.value))
      (pom \\ "dependencies")
        .map(_ \ "dependency")
        .find(dependency =>
          (dependency \ "groupId").text == "com.sksamuel.scapegoat" &&
            (dependency \ "artifactId").text == "scalac-scapegoat-plugin",
        )
        .foreach(_ => sys.error("scapegoat compiler plugin is not excluded"))
    },
  )
