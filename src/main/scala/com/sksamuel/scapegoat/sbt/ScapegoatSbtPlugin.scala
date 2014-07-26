package com.sksamuel.scapegoat.sbt

import sbt._
import Keys._

/** @author Stephen Samuel */
object ScapegoatSbtPlugin extends AutoPlugin {

  val GroupId = "com.sksamuel.scapegoat"
  val ArtifactId = "scalac-scapegoat-plugin"
  val Version = "0.2.0"

  lazy val scapegoatCompile = config("scapegoat-compile") extend Compile

  object autoImport {
    lazy val scapegoat = taskKey[Unit]("scapegoat")
  }

  import autoImport._

  override def trigger = allRequirements
  override lazy val projectSettings = Seq(
    libraryDependencies += {
      GroupId % (ArtifactId + "_" + scalaBinaryVersion.value) % Version % Compile.name
    },
    scalacOptions in Compile ++= {
        // find all deps for the compile scope
        val scapegoatDependencies = update.value matching configurationFilter(Compile.name)
        // ensure we have the scapegoat dependency on the classpath and if so add it as a scalac plugin
        scapegoatDependencies.find(_.getAbsolutePath.contains(ArtifactId)) match {
          case None => throw new Exception(s"Fatal: $ArtifactId not in libraryDependencies")
          case Some(classpath) =>
            val target = crossTarget.value
            streams.value.log.info(s"[scapegoat] will write data to [$target]")
            Seq(
              "-Xplugin:" + classpath.getAbsolutePath,
              "-P:scapegoat:dataDir:" + target.getAbsolutePath + "/scoverage-data"
            )
        }
    },
    scapegoat := {
      val analyis = (compile in Compile).value
      streams.value.log.info("[scapegoat] generating report")
      ()
    }
  )
}
