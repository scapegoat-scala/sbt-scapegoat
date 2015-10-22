package com.sksamuel.scapegoat.sbt

import sbt._
import sbt.Keys._

/** @author Stephen Samuel */
object ScapegoatSbtPlugin extends AutoPlugin {

  val GroupId = "com.sksamuel.scapegoat"
  val ArtifactId = "scalac-scapegoat-plugin"

  object autoImport {
    val Scapegoat = config("scapegoat") extend Compile

    lazy val scapegoat = taskKey[Unit]("Run scapegoat quality checks")
    lazy val scapegoatVersion = settingKey[String]("The version of the scala plugin to use")
    lazy val scapegoatDisabledInspections = settingKey[Seq[String]]("Inspections that are disabled globally, by simple name")
    lazy val scapegoatEnabledInspections = settingKey[Seq[String]]("Inspections that are explicitly enabled, by simple name")
    lazy val scapegoatCustomInspectionsClasspath = taskKey[Seq[File]](
      "Classpaths from which to load custom inspections. " +
        "The classpaths will be scanned for classes which implement " +
        "`com.sksamuel.scapegoat.Inspection` using the ServiceLoader API " +
        "(i.e. the full classname must be listed in a resource of name " +
        "`META-INF/services/com.sksamuel.scapegoat.Inspection`), " +
        "and any found will be added to the scapegoat checks. " +
        "If you also set scapegoatCustomInspectionsDependencies, be sure to " +
        "use '+=' not ':=' to set this unless you want to disable the " +
        "automatic addition of those dependencies to this classpath.")
    lazy val scapegoatCustomInspectionsDependencies = settingKey[Seq[sbt.ModuleID]](
      "Ivy dependencies containing custom inspections. " +
        "The dependencies will be scanned for classes which implement " +
        "`com.sksamuel.scapegoat.Inspection` using the ServiceLoader API " +
        "(i.e. the full classname must be listed in a resource of name " +
        "`META-INF/services/com.sksamuel.scapegoat.Inspection`), " +
        "and any found will be added to the scapegoat checks. " +
        "(If the inspection libraries have any transitive dependencies " +
        "which the compiler will need, you currently have to manually add those " +
        "to scapegoatCustomInspectionsClasspath.)")
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
  override def projectSettings = {
    inConfig(Scapegoat) {
      Defaults.compileSettings ++
      Seq(
        sources := (sources in Compile).value,
        resources := (resources in Compile).value,
        resourceDirectories := (resourceDirectories in Compile).value,
        scalacOptions := {
          // Ensure we have the scapegoat dependency on the classpath and
          // add it as a scalac plugin:
          val scapegoatDeps = (update in Scapegoat).value
            .select(module = (m: ModuleID) =>
              m.organization == GroupId && m.name.startsWith(ArtifactId))

          scapegoatDeps match {
            case Seq() => throw new Exception(s"Fatal: $GroupId.$ArtifactId not in libraryDependencies")
            case Seq(classpath) =>

              val verbose = scapegoatVerbose.value
              val path = scapegoatOutputPath.value
              val reports = scapegoatReports.value

              if (verbose)
                streams.value.log.info(s"[scapegoat] setting output dir to [$path]")

              val disabled = scapegoatDisabledInspections.value.filterNot(_.trim.isEmpty)
              if (disabled.nonEmpty && verbose)
                streams.value.log.info("[scapegoat] disabled inspections: " + disabled.mkString(","))

              val enabled = scapegoatEnabledInspections.value.filterNot(_.trim.isEmpty)
              if (enabled.nonEmpty && verbose)
                streams.value.log.info("[scapegoat] enabled inspections: " + enabled.mkString(","))

              val customCp = scapegoatCustomInspectionsClasspath.value.map(_.toURI.toString)
              if (customCp.nonEmpty && verbose)
                streams.value.log.info("[scapegoat] custom inspections classpath: " + customCp.mkString(";"))

              val ignoredFilePatterns = scapegoatIgnoredFiles.value.filterNot(_.trim.isEmpty)
              if (ignoredFilePatterns.nonEmpty && verbose)
                streams.value.log.info("[scapegoat] ignored file patterns: " + ignoredFilePatterns.mkString(","))

              (scalacOptions in Compile).value ++ Seq(
                Some("-Xplugin:" + classpath.getAbsolutePath),
                Some("-P:scapegoat:verbose:" + scapegoatVerbose.value),
                Some("-P:scapegoat:consoleOutput:" + scapegoatConsoleOutput.value),
                Some("-P:scapegoat:dataDir:" + path),
                // FIXME: the corresponding option in scalac-scapegoat has a different name!
                if (disabled.isEmpty) None else Some("-P:scapegoat:disabledInspections:" + disabled.mkString(":")),
                // FIXME: no corresponding 'enabledInspections' option in scalac-scapegoat!
                if (enabled.isEmpty) None else Some("-P:scapegoat:enabledInspections:" + enabled.mkString(":")),
                if (customCp.isEmpty) None else Some("-P:scapegoat:customInspectionsClasspath:" + customCp.mkString(";")),
                if (ignoredFilePatterns.isEmpty) None else Some("-P:scapegoat:ignoredFiles:" + ignoredFilePatterns.mkString(":")),
                if (reports.isEmpty) None else Some("-P:scapegoat:reports:" + reports.mkString(":"))
              ).flatten
          }
        }
      )
    } ++ Seq(
      scapegoat := {
        clean.value
        // (products is compile + copy resources)
        (products in Scapegoat).value
      },
      scapegoatVersion := "1.1.0",
      scapegoatConsoleOutput := true,
      scapegoatVerbose := true,
      scapegoatMaxInfos := -1,
      scapegoatMaxWarnings := -1,
      scapegoatMaxErrors := -1,
      scapegoatDisabledInspections := Nil,
      scapegoatEnabledInspections := Nil,
      scapegoatCustomInspectionsDependencies := Nil,
      /**
       * The custom inspections classpath includes, by default, all the
       * JARs from [[scapegoatCustomInspectionsDependencies]].
       *
       * @see [[scapegoatCustomInspections]] which will scan each of these
       *      classpath entries for implementations of Inspection.
       */
      scapegoatCustomInspectionsClasspath := {

        val resolvedDeps = (update in Scapegoat).value
        val crossVersioner = CrossVersion.apply(
          (scalaVersion in Scapegoat).value, (scalaBinaryVersion in Scapegoat).value)

        scapegoatCustomInspectionsDependencies.value.flatMap { modId =>

          // The CI dep may have been cross-versioned by Ivy:
          val expectedModId = crossVersioner(modId)
          resolvedDeps
            .select(module = (m: ModuleID) =>
            // Compare the "toString"s of the ModuleID, otherwise config differences
            // like the CrossVersion settings will fail the comparison:
            m.toString() == expectedModId.toString())
            .ensuring(
              _.nonEmpty,
              s"No resolved Ivy artifacts found for $expectedModId")
        }
      },
      scapegoatIgnoredFiles := Nil,
      scapegoatOutputPath := (crossTarget in Compile).value.getAbsolutePath + "/scapegoat-report",
      scapegoatReports := Seq("all"),
      libraryDependencies ++= {
        // All "scapegoatCustomInspectionsDependencies" and the plugin itself
        // need to be added as "provided" (compile-time only) dependencies so that Ivy
        // downloads the JARs and we have the classpath dir in the update report.
        // This is then used by 'scapegoatCustomInspectionsClasspath' above.
        val customDeps = scapegoatCustomInspectionsDependencies.value.map(_ % Provided.name)

        // Add the plugin itself as a library dependency of the project, so
        // that the support classes (Inspection etc.) are available at compile
        // time.
        // FIXME: "Compile" seems like the wrong scope here, as it implies a
        // runtime dependency (despite the name). These deps are compile-only,
        // so the correct scope ought to be "Provided", I think.
        // However, that doesn't actually work.
        val pluginDep = GroupId % (ArtifactId + "_" + scalaBinaryVersion.value) % (scapegoatVersion in Scapegoat).value % Compile.name

        customDeps :+ pluginDep
      }
    )
  }
}
