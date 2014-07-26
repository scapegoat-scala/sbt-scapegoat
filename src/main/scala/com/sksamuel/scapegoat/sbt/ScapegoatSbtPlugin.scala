package com.sksamuel.scapegoat.sbt

import sbt._
import sbt.Keys._

/** @author Stephen Samuel */
object ScapegoatSbtPlugin extends AutoPlugin {

  val GroupId = "com.sksamuel.scapegoat"
  val ArtifactId = "scalac-scapegoat-plugin"
  val Version = "0.2.0"

  override def trigger = allRequirements
  override lazy val projectSettings = Seq(
    libraryDependencies ++= Seq(
      GroupId % (ArtifactId + "_" + scalaBinaryVersion.value) % Version % Compile.name
    ),
    scalacOptions in(Compile, compile) ++= {
      // find all deps for the compile scope
      val scapegoatDependencies = update.value matching configurationFilter(Compile.name)
      // ensure we have the scapegoat dependency on the classpath and if so add it as a scalac plugin
      scapegoatDependencies.find(_.getAbsolutePath.contains(ArtifactId)) match {
        case None => throw new Exception(s"Fatal: $ArtifactId not in libraryDependencies")
        case Some(classpath) =>
          val target = (crossTarget in Compile).value.getAbsolutePath + "/scapegoat-report"
          streams.value.log.info(s"[scapegoat] setting output dir to [$target]")
          Seq(
            "-Xplugin:" + classpath.getAbsolutePath,
            "-P:scapegoat:dataDir:" + target
          )
      }
    }
  )
}
