package com.sksamuel.scapegoat.sbt

import sbt._
import sbt.Keys._

import scala.language.postfixOps

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
    lazy val scapegoatEnabledInspections = settingKey[Seq[String]]("Inspections that are enabled globally")
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
    lazy val scapegoatMinimalWarnLevel = settingKey[String]("minimal level of inspection to be displayed in reports.")
  }

  import autoImport._

  def doScapegoatClean(force: Boolean, classesDir: File, log: Logger) {
    if (force) {
      log.info(s"[scapegoat] Removing scapegoat class directory: $classesDir")
      IO.delete(Seq(classesDir))
    }
  }

  override def trigger = allRequirements

  override def buildSettings: Seq[Def.Setting[_]] = super.buildSettings ++ Seq(
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
    scapegoatSourcePrefix := "src/main/scala",
    scapegoatMinimalWarnLevel := "info")

  override def projectSettings: Seq[Def.Setting[_]] = {
    inConfig(Scapegoat) {
      Defaults.compileSettings ++
        Seq(
          sources := (Compile / sources).value,
          managedClasspath := (Compile / managedClasspath).value,
          unmanagedClasspath := (Compile / unmanagedClasspath).value,
          scalacOptions := {
            // find all deps for the compile scope
            val scapegoatDependencies = (Scapegoat / update).value matching configurationFilter(Provided.name)
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

                // disabledInspections takes precedence over enabledInspections
                val enabled = scapegoatEnabledInspections.value.filterNot(s => s.trim.isEmpty)
                if (enabled.nonEmpty && verbose)
                  streamsValue.log.info("[scapegoat] enabled inspections: " + enabled.mkString(","))

                val ignoredFilePatterns = scapegoatIgnoredFiles.value.filterNot(_.trim.isEmpty)
                if (ignoredFilePatterns.nonEmpty && verbose)
                  streamsValue.log.info("[scapegoat] ignored file patterns: " + ignoredFilePatterns.mkString(","))

                val customSourcePrefix = scapegoatSourcePrefix.value
                if (customSourcePrefix.nonEmpty)
                  streamsValue.log.info("[scapegoat] source prefix: " + customSourcePrefix)

                val customMinimalWarnLevel = scapegoatMinimalWarnLevel.value
                if (customMinimalWarnLevel.nonEmpty)
                  streamsValue.log.info("[scapegoat] minimal warn level: " + customMinimalWarnLevel)

                (Compile / scalacOptions).value ++ Seq(
                  Some("-Xplugin:" + classpath.getAbsolutePath),
                  Some("-P:scapegoat:verbose:" + scapegoatVerbose.value),
                  Some("-P:scapegoat:consoleOutput:" + scapegoatConsoleOutput.value),
                  Some("-P:scapegoat:dataDir:" + path),
                  if (enabled.isEmpty) None else Some("-P:scapegoat:enabledInspections:" + enabled.mkString(":")),
                  if (disabled.isEmpty) None else Some("-P:scapegoat:disabledInspections:" + disabled.mkString(":")),
                  if (ignoredFilePatterns.isEmpty) None else Some("-P:scapegoat:ignoredFiles:" + ignoredFilePatterns.mkString(":")),
                  if (reports.isEmpty) None else Some("-P:scapegoat:reports:" + reports.mkString(":")),
                  if (customSourcePrefix.isEmpty) None else Some(s"-P:scapegoat:sourcePrefix:$customSourcePrefix"),
                  if (customMinimalWarnLevel.isEmpty) None else Some(s"-P:scapegoat:minimalLevel:$customMinimalWarnLevel")).flatten
            }
          })
    } ++ Seq(
      (Scapegoat / compile) := ((Scapegoat / compile) dependsOn scapegoatClean).value,
      scapegoat := (Scapegoat / compile).value,
      scapegoatCleanTask := doScapegoatClean((ThisBuild / scapegoatRunAlways).value, (Scapegoat / classDirectory).value, streams.value.log),
      scapegoatClean := doScapegoatClean(force = true, (Scapegoat / classDirectory).value, streams.value.log),
      // FIXME Cannot seem to make this a build setting (compile:crossTarget is an undefined setting)
      scapegoatOutputPath := (Compile / crossTarget).value.getAbsolutePath + "/scapegoat-report",
      libraryDependencies ++= {
        val selectedScapegoatVersion = (scapegoatVersion?).value.getOrElse {
          scalaVersion.value match {
            // To give a better out of the box experience, default to a recent version of Scapegoat for known Scala versions
            case "2.13.10" | "2.13.9" | "2.12.17" | "2.12.16" => "2.1.1"
            // Default to the latest version with Scala 2.11 support to improve apparent compatibility
            case _ => "1.4.17"
          }
        }
        Seq(crossVersion(GroupId %% ArtifactId % selectedScapegoatVersion % Provided))
      })
  }

  private def crossVersion(mod: ModuleID) = {
    val components = mod.revision.split('.').take(2).map { c => try { c.toInt } catch { case _: Exception => 0 } }

    components match {
      // versions >= 1.4.0 use the full cross version
      case Array(major, minor) if major > 1 || minor >= 4 => mod cross CrossVersion.full
      case _ => mod
    }
  }
}
