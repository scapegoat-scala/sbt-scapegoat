package com.sksamuel.scapegoat.sbt

import sbt._
import sbt.Keys._

/** @author Stephen Samuel */
object ScapegoatSbtPlugin extends AutoPlugin {

  val GroupId = "com.sksamuel.scapegoat"
  val ArtifactId = "scalac-scapegoat-plugin"
  val Version = "0.90.6"

  object autoImport {
    lazy val disabledInspections = settingKey[Seq[String]]("Inspections that are disabled globally")
    lazy val scapegoatMaxErrors = settingKey[Int]("Maximum number of errors before the build will fail")
    lazy val scapegoatMaxWarnings = settingKey[Int]("Maximum number of warnings before the build will fail")
    lazy val consoleOutput = settingKey[Boolean]("Output results of scan to the console during compilation")
    lazy val outputPath = settingKey[String]("Directory where reports will be written")
  }

  import autoImport._

  override def trigger = allRequirements
  override lazy val projectSettings = Seq(
    disabledInspections := Nil,
    libraryDependencies ++= Seq(
      GroupId % (ArtifactId + "_" + scalaBinaryVersion.value) % Version
    ),
    consoleOutput := true,
    outputPath := (crossTarget in Compile).value.getAbsolutePath + "/scapegoat-report",
    scalacOptions in(Compile, compile) ++= {
      // find all deps for the compile scope
      val scapegoatDependencies = update.value matching configurationFilter(Compile.name)
      // ensure we have the scapegoat dependency on the classpath and if so add it as a scalac plugin
      scapegoatDependencies.find(_.getAbsolutePath.contains(ArtifactId)) match {
        case None => throw new Exception(s"Fatal: $ArtifactId not in libraryDependencies")
        case Some(classpath) =>
          val path = outputPath.value
          streams.value.log.info(s"[scapegoat] setting output dir to [$path]")
          val disabled = disabledInspections.value
          if (disabled.size > 0)
            streams.value.log.info(s"[scapegoat] disabling inspections: " + disabled.mkString(","))
          Seq(
            "-Xplugin:" + classpath.getAbsolutePath,
            "-P:scapegoat:consoleOutput:" + consoleOutput.value,
            "-P:scapegoat:dataDir:" + path,
            "-P:scapegoat:disabledInspections:" + disabled.mkString(":")
          )
      }
    }
  )
}
