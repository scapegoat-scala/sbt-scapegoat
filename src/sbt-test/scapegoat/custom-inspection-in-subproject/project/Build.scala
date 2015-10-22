import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin.autoImport._
import sbt.Keys._
import sbt._

object CustomInspectionInSubproject extends Build {

  val scalaV = "2.11.6"

  lazy val project = Project("CustomInspectionInSubproject", file("."))
    // "aggregate" causes these child projects to be built and tested whenever
    // the parent project is built and tested.
    .aggregate(inspections)
    .settings(
      scalaVersion := scalaV,
      publishLocal := {},

      scapegoatCustomInspectionsClasspath +=
        (classDirectory in inspections in Scapegoat).value)

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
      organization  := "com.sksamuel.scapegoat.examples",
      version := "1.0.0",
      libraryDependencies ++= List(
        "com.sksamuel.scapegoat" %% "scalac-scapegoat-plugin" % "1.1.0",
        "org.scala-lang"         %  "scala-compiler"          % scalaV,
        "org.scalatest"          %% "scalatest"               % "2.2.4" % "test"),
      scalaVersion := scalaV)
}
