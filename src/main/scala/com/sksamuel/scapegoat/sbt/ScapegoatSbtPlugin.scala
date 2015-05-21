package com.sksamuel.scapegoat.sbt

import sbt._
import sbt.Keys._

/** @author Stephen Samuel */
object ScapegoatSbtPlugin extends AutoPlugin {

  val GroupId = "com.sksamuel.scapegoat"
  val ArtifactId = "scalac-scapegoat-plugin"

  object autoImport {
    lazy val scapegoatVersion = settingKey[String]("The version of the scala plugin to use")
    lazy val scapegoatDisabledInspections = settingKey[Seq[String]]("Inspections that are disabled globally")
    lazy val scapegoatEnabledInspections = settingKey[Seq[String]]("Inspections that are explicitly enabled")
    lazy val scapegoatIgnoredFiles = settingKey[Seq[String]]("File patterns to ignore")
    lazy val scapegoatMaxErrors = settingKey[Int]("Maximum number of errors before the build will fail")
    lazy val scapegoatMaxWarnings = settingKey[Int]("Maximum number of warnings before the build will fail")
    lazy val scapegoatMaxInfos = settingKey[Int]("Maximum number of infos before the build will fail")
    lazy val scapegoatConsoleOutput = settingKey[Boolean]("Output results of scan to the console during compilation")
    lazy val scapegoatOutputPath = settingKey[String]("Directory where reports will be written")
    lazy val scapegoatVerbose = settingKey[Boolean]("Verbose mode for inspections")
    lazy val scapegoatReports = settingKey[Seq[String]]("The report styles to generate")
  }

  import autoImport._

  override def trigger = allRequirements
  override def projectSettings = Seq(
    scapegoatVersion := "0.94.7",
    libraryDependencies ++= Seq(
      GroupId % (ArtifactId + "_" + scalaBinaryVersion.value) % scapegoatVersion.value % Compile.name
    ),
    scapegoatConsoleOutput := true,
    scapegoatVerbose := true,
    scapegoatMaxInfos := -1,
    scapegoatMaxWarnings := -1,
    scapegoatMaxErrors := -1,
    scapegoatDisabledInspections := Nil,
    scapegoatEnabledInspections := Nil,
    scapegoatIgnoredFiles := Nil,
    scapegoatOutputPath := (crossTarget in Compile).value.getAbsolutePath + "/scapegoat-report",
    scapegoatReports := Seq("all"),
    scalacOptions in(Compile, compile) ++= {
      // find all deps for the compile scope
      val scapegoatDependencies = update.value matching configurationFilter(Compile.name)
      // ensure we have the scapegoat dependency on the classpath and if so add it as a scalac plugin
      scapegoatDependencies.find(_.getAbsolutePath.contains(ArtifactId)) match {
        case None => throw new Exception(s"Fatal: $ArtifactId not in libraryDependencies")
        case Some(classpath) =>

          val verbose = scapegoatVerbose.value
          val path = scapegoatOutputPath.value
          val reports = scapegoatReports.value
          if (verbose)
            streams.value.log.info(s"[scapegoat] setting output dir to [$path]")

          val disabled = scapegoatDisabledInspections.value.filterNot(_.trim.isEmpty)
          if (disabled.size > 0 && verbose)
            streams.value.log.info("[scapegoat] disabled inspections: " + disabled.mkString(","))

          val enabled = scapegoatEnabledInspections.value.filterNot(_.trim.isEmpty)
          if (enabled.size > 0 && verbose)
            streams.value.log.info("[scapegoat] enabled inspections: " + enabled.mkString(","))

          val ignoredFilePatterns = scapegoatIgnoredFiles.value.filterNot(_.trim.isEmpty)
          if (ignoredFilePatterns.size > 0 && verbose)
            streams.value.log.info("[scapegoat] ignored file patterns: " + ignoredFilePatterns.mkString(","))

          Seq(
            Some("-Xplugin:" + classpath.getAbsolutePath),
            Some("-P:scapegoat:verbose:" + scapegoatVerbose.value),
            Some("-P:scapegoat:consoleOutput:" + scapegoatConsoleOutput.value),
            Some("-P:scapegoat:dataDir:" + path),
            if (disabled.isEmpty) None else Some("-P:scapegoat:disabledInspections:" + disabled.mkString(":")),
            if (enabled.isEmpty) None else Some("-P:scapegoat:enabledInspections:" + enabled.mkString(":")),
            if (ignoredFilePatterns.isEmpty) None else Some("-P:scapegoat:ignoredFiles:" + ignoredFilePatterns.mkString(":"))
            if (scapegoatReports.isEmpty) None else Some("-P:scapegoat:reports:" + reports.mkString(":")
          ).flatten
      }
    }
    //    (compile in Compile) := {
    //      val analysis = (compile in Compile).value
    //      val xmlPath = scapegoatOutputPath.value + "/scapegoat.xml"
    //      val xml = scala.xml.XML.loadFile(xmlPath)
    //      val infos = (xml \ "@infos").text.toInt
    //      val warns = (xml \ "@warns").text.toInt
    //      val errors = (xml \ "@errors").text.toInt
    //
    //      val maxInfos = scapegoatMaxInfos.value
    //      val maxWarns = scapegoatMaxWarnings.value
    //      val maxErrors = scapegoatMaxErrors.value
    //
    //      if (maxErrors >= 0 && infos > maxErrors) {
    //        streams.value.log.info(s"[scapegoat] Build failed due to max errors exceed [errors=$errors, max=$maxErrors]")
    //        throw new RuntimeException("Aborting build")
    //      }
    //
    //      if (maxWarns >= 0 && infos > maxWarns) {
    //        streams.value.log.info(s"[scapegoat] Build failed due to max warnings exceed [warns=$warns, max=$maxWarns]")
    //        throw new RuntimeException("Aborting build")
    //      }
    //
    //      if (maxInfos >= 0 && infos > maxInfos) {
    //        streams.value.log.info(s"[scapegoat] Build failed due to max infos exceed [infos=$infos, max=$maxInfos]")
    //        throw new RuntimeException("Aborting build")
    //      }
    //
    //      analysis
    //    }
  )
}
