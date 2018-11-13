package com.sksamuel.scapegoat.sbt

import sbt._
import sbt.Keys._

object ScapegoatSbtPlugin extends AutoPlugin {

  val GroupId = "com.sksamuel.scapegoat"
  val ArtifactId = "scalac-scapegoat-plugin"

  object autoImport {
    val Scapegoat = config("scapegoat") extend Compile

    lazy val scapegoat = taskKey[Unit]("Run scapegoat quality checks")
    lazy val scapegoatCleanTask = taskKey[Unit]("Conditionally clean the scapegoat output directories")
    lazy val scapegoatClean = taskKey[Unit]("Clean the scapegoat output directories")
    lazy val scapegoatVersion = settingKey[String]("The version of the scala plugin to use")
    lazy val scapegoatDisabledInspections = settingKey[Seq[String]]("Inspections that are disabled globally")
    lazy val scapegoatEnabledInspections = settingKey[Seq[String]]("Inspections that are explicitly enabled")
    lazy val scapegoatRunAlways = settingKey[Boolean]("Force inspections to run even on files that haven't changed")
    lazy val scapegoatIgnoredFiles = settingKey[Seq[String]]("File patterns to ignore")
    lazy val scapegoatMaxErrors = settingKey[Int]("Maximum number of errors before the build will fail")
    lazy val scapegoatMaxWarnings = settingKey[Int]("Maximum number of warnings before the build will fail")
    lazy val scapegoatMaxInfos = settingKey[Int]("Maximum number of infos before the build will fail")
    lazy val scapegoatConsoleOutput = settingKey[Boolean]("Output results of scan to the console during compilation")
    lazy val scapegoatOutputPath = settingKey[String]("Directory where reports will be written")
    lazy val scapegoatVerbose = settingKey[Boolean]("Verbose mode for inspections")
    lazy val scapegoatReports = settingKey[Seq[String]]("The report styles to generate")
    lazy val scapegoatSourcePrefix = settingKey[String]("Package root directory, for ex. 'app' for Play applications")
  }

  import autoImport._

  def doScapegoatClean(force: Boolean, classesDir: File, log: Logger) {
    if (force) {
      log.info(s"[scapegoat] Removing scapegoat class directory: $classesDir")
      IO.delete(Seq(classesDir))
    }
  }

  override def trigger = allRequirements

  override def buildSettings = super.buildSettings ++ Seq(
    scapegoatVersion := "1.0.0",
    scapegoatRunAlways := true,
    scapegoatConsoleOutput := true,
    scapegoatVerbose := true,
    scapegoatMaxInfos := -1,
    scapegoatMaxWarnings := -1,
    scapegoatMaxErrors := -1,
    scapegoatDisabledInspections := Nil,
    scapegoatEnabledInspections := Nil,
    scapegoatIgnoredFiles := Nil,
    scapegoatReports := Seq("all"),
    scapegoatSourcePrefix := "src/main/scala")

  override def projectSettings = {
    inConfig(Scapegoat) {
      Defaults.compileSettings ++
        Seq(
          sources := (sources in Compile).value,
          managedClasspath := (managedClasspath in Compile).value,
          unmanagedClasspath := (unmanagedClasspath in Compile).value,
          scalacOptions := {
            // find all deps for the compile scope
            val scapegoatDependencies = (update in Scapegoat).value matching configurationFilter(Provided.name)
            // ensure we have the scapegoat dependency on the classpath and if so add it as a scalac plugin
            scapegoatDependencies.find(_.getAbsolutePath.contains(ArtifactId)) match {
              case None => throw new Exception(s"Fatal: $ArtifactId not in libraryDependencies ($scapegoatDependencies)")
              case Some(classpath) =>

                val verbose = scapegoatVerbose.value
                val path = scapegoatOutputPath.value
                val reports = scapegoatReports.value
                val streamsValue = streams.value

                if (verbose)
                  streamsValue.log.info(s"[scapegoat] setting output dir to [$path]")

                val disabled = scapegoatDisabledInspections.value.filterNot(_.trim.isEmpty)
                if (disabled.nonEmpty && verbose)
                  streamsValue.log.info("[scapegoat] disabled inspections: " + disabled.mkString(","))

                val enabled = scapegoatEnabledInspections.value.filterNot(_.trim.isEmpty)
                if (enabled.nonEmpty && verbose)
                  streamsValue.log.info("[scapegoat] enabled inspections: " + enabled.mkString(","))

                val ignoredFilePatterns = scapegoatIgnoredFiles.value.filterNot(_.trim.isEmpty)
                if (ignoredFilePatterns.nonEmpty && verbose)
                  streamsValue.log.info("[scapegoat] ignored file patterns: " + ignoredFilePatterns.mkString(","))

                val customSourcePrefix = scapegoatSourcePrefix.value
                if (customSourcePrefix.nonEmpty)
                  streamsValue.log.info("[scapegoat] source prefix: " + customSourcePrefix)

                (scalacOptions in Compile).value ++ Seq(
                  Some("-Xplugin:" + classpath.getAbsolutePath),
                  Some("-P:scapegoat:verbose:" + scapegoatVerbose.value),
                  Some("-P:scapegoat:consoleOutput:" + scapegoatConsoleOutput.value),
                  Some("-P:scapegoat:dataDir:" + path),
                  if (disabled.isEmpty) None else Some("-P:scapegoat:disabledInspections:" + disabled.mkString(":")),
                  if (enabled.isEmpty) None else Some("-P:scapegoat:enabledInspections:" + enabled.mkString(":")),
                  if (ignoredFilePatterns.isEmpty) None else Some("-P:scapegoat:ignoredFiles:" + ignoredFilePatterns.mkString(":")),
                  if (reports.isEmpty) None else Some("-P:scapegoat:reports:" + reports.mkString(":")),
                  if (customSourcePrefix.isEmpty) None else Some("-P:scapegoat:sourcePrefix:" + customSourcePrefix)).flatten
            }
          })
    } ++ Seq(
      (compile in Scapegoat) := ((compile in Scapegoat) dependsOn scapegoatClean).value,
      scapegoat := (compile in Scapegoat).value,
      scapegoatCleanTask := doScapegoatClean((scapegoatRunAlways in ThisBuild).value, (classDirectory in Scapegoat).value, streams.value.log),
      scapegoatClean := doScapegoatClean(true, (classDirectory in Scapegoat).value, streams.value.log),
      // FIXME Cannot seem to make this a build setting (compile:crossTarget is an undefined setting)
      scapegoatOutputPath := (crossTarget in Compile).value.getAbsolutePath + "/scapegoat-report",
      libraryDependencies ++= Seq(GroupId %% ArtifactId % (scapegoatVersion in ThisBuild).value % Provided))
  }
}
