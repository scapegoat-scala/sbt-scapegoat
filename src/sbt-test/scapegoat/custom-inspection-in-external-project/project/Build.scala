import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin.autoImport._
import sbt.Keys._
import sbt._

object CustomInspectionInExternalProject extends Build {

  val scalaV = "2.11.6"

  lazy val project = Project("CustomInspectionInExternalProject", file("."))
    .settings(
      scalaVersion := scalaV,

      scapegoatCustomInspectionsDependencies +=
        "com.sksamuel.scapegoat.examples" %% "inspections" % "1.0.0")
}
