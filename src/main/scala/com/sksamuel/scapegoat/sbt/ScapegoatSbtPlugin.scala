package com.sksamuel.scapegoat.sbt

import sbt.Keys._
import sbt._

/** @author Stephen Samuel */
object ScapegoatSbtPlugin extends AutoPlugin {

  val GroupId = "com.sksamuel.scapegoat"
  val ArtifactId = "scalac-scapegoat-plugin"
  val Version = "0.2.0"

  object autoImport {
    lazy val scapegoat = taskKey[Boolean]("scapegoat")
  }

  import autoImport._

  override lazy val buildSettings = Seq(
    libraryDependencies += {
      GroupId % (ArtifactId + "_" + scalaBinaryVersion.value) % Version % Compile.name
    },
    scalacOptions in Compile <++= update map {
      (report) =>
        // find all deps for this configuration
        val scapegoatDependencies = report matching configurationFilter(Compile.name)
        // ensure we have the scapegoat dependency on the classpath and if so add it as a scalac plugin
        scapegoatDependencies.find(_.getAbsolutePath.contains(ArtifactId)) match {
          case None => throw new Exception(s"Fatal: $ArtifactId not in libraryDependencies")
          case Some(classpath) =>
            Seq(
              "-Xplugin:" + classpath.getAbsolutePath
            )
        }
    },
    scapegoat := {
      val analyis = (compile in Compile).value
      streams.value.log.info("[scapegoat] generating report")
      true
    }
  )
}
