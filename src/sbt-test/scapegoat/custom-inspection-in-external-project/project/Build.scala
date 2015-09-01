import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin.autoImport._
import sbt.Keys._
import sbt._

object CustomInspectionInExternalProject extends Build {

  val scalaV = "2.11.6"

  lazy val project = Project("CustomInspectionInExternalProject", file("."))
    .settings(
      scalaVersion := scalaV,

      // FIXME: Having to list the classes here is error-prone.
      // There are two main cases:
      //   1. When the inspection lib is in the same build, then SBT might be able to enumerate the
      //      classes here if we depend on the inspections/compile Task.
      //      However, SBT 'settings' are not allowed to depend on SBT 'tasks', so that might not work.
      //   2. When the inspection lib is externally sourced, we might be able to grab it from the
      //      JAR metadata somehow? Do we need to fix an API for scapegoat plugins?
      scapegoatCustomInspections := List(
        "my.inspections.DisallowJavaDateConstruction"),

      scapegoatCustomInspectionsDependencies +=
        "com.sksamuel.scapegoat.examples" %% "inspections" % "1.0.0")
}
