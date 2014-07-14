package com.sksamuel.scapegoat.sbt

import sbt.Keys._
import sbt._

/** @author Stephen Samuel */
object ScapegoatSbtPlugin extends sbt.Plugin {

  val OrgScoverage = "com.sksamuel"
  val ScalacArtifact = "scalac-scapegoat-plugin"
  val ScoverageVersion = "0.1.0"

  object Keys {
  }

  val ScapegoatCompile = config("scapegoat") extend Compile

  lazy val scapegoatSettings: Seq[Setting[_]] = {
    inConfig(ScapegoatCompile)(Defaults.compileSettings) ++
      Seq(
        sources in ScapegoatCompile <<= (sources in Compile),
        sourceDirectory in ScapegoatCompile <<= (sourceDirectory in Compile),
        resourceDirectory in ScapegoatCompile <<= (resourceDirectory in Compile),
        resourceGenerators in ScapegoatCompile <<= (resourceGenerators in Compile),
        externalDependencyClasspath in ScapegoatCompile <<= Classpaths
          .concat(externalDependencyClasspath in ScapegoatCompile, externalDependencyClasspath in Compile),
        internalDependencyClasspath in ScapegoatCompile <<= (internalDependencyClasspath in Compile),
        scalacOptions in ScapegoatCompile <++= update map {
          (report) =>
            // find all deps for this configuration
            val scapegoatDependencies = report matching configurationFilter(ScapegoatCompile.name)
            // ensure we have the scapegoat dependency on the classpath and if so add it as a scalac plugin
            scapegoatDependencies.find(_.getAbsolutePath.contains(ScalacArtifact)) match {
              case None => throw new Exception(s"Fatal: $ScalacArtifact not in libraryDependencies")
              case Some(classpath) =>
                Seq(
                  "-Xplugin:" + classpath.getAbsolutePath
                )
            }
        },
        compile in Compile <<= (compile in Compile) map { analysis =>
          println("[scapegoat] generating report")
          analysis
        }
      )
  }
}
