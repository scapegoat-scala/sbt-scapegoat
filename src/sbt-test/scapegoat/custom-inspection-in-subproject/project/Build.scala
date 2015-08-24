import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin.autoImport._
import sbt.Keys._
import sbt._

object ScapegoatCustomInspectionsExample extends Build {

  val scalaV = "2.11.6"

  lazy val project = Project("ScapegoatCustomInspectionsExample", file("."))
    // "aggregate" causes these child projects to be built and tested whenever
    // the parent project is built and tested.
    .aggregate(inspections)
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

      scapegoatCustomInspectionsClasspath := List(
        (classDirectory in inspections in Scapegoat).value))

    // FIXME: this dependency ought to be in scope 'provided', as it is
    // compile only, so is not needed at runtime.
    // Unfortunately, the current definition of the SBT 'scapegoat' task
    // includes a "clean", which means that SBT will delete the compiled
    // inspection classes just before attempting to run the inspections.
    // If the dependency here is "compile" then SBT knows to rebuild the
    // inspections before running them, however it does not add that
    // task dependency if this is marked "provided". I think that is a
    // bug in SBT.
    .dependsOn(inspections)

  lazy val inspections = Project("inspections", file("inspections"))
    .settings(
      libraryDependencies ++= List(
        "com.sksamuel.scapegoat" %% "scalac-scapegoat-plugin" % "1.1.0",
        "org.scala-lang"         %  "scala-compiler"          % scalaV,
        "org.scalatest"          %% "scalatest"               % "2.2.4" % "test"),
      scalaVersion := scalaV)
}
