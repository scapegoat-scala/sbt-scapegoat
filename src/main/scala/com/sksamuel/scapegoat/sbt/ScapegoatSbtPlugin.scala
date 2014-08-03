package com.sksamuel.scapegoat.sbt

import sbt._
import sbt.Keys._

/** @author Stephen Samuel */
object ScapegoatSbtPlugin extends AutoPlugin {

  val GroupId = "com.sksamuel.scapegoat"
  val ArtifactId = "scalac-scapegoat-plugin"
  val Version = "0.90.0"

  object autoImport {
    lazy val disabledInspections = settingKey[Seq[String]]("Inspections that are disabled globally")
  }

  import autoImport._

  override def trigger = allRequirements
  override lazy val projectSettings = Seq(
    disabledInspections := Nil,
    autoCompilerPlugins := true,
    libraryDependencies ++= Seq(
      compilerPlugin(GroupId % (ArtifactId + "_" + scalaBinaryVersion.value) % Version)
    ),
    scalacOptions in(Compile, compile) ++= {
      Seq(
        "-P:scapegoat:dataDir:" + target,
        "-P:scapegoat:disabledInspections:" + disabledInspections.value.mkString(",")
      )
    }
  )
}
