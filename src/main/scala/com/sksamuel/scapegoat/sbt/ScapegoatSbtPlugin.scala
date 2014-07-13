package com.sksamuel.scapegoat.sbt

import sbt._

/** @author Stephen Samuel */
class ScapegoatSbtPlugin extends Plugin {

  object Keys {
    lazy val scapegoat = taskKey[Boolean]("run scapegoat")
  }

  import Keys._

  val scapegoatSettings: Seq[Setting[_]] = {
    scapegoat := {
      true
    }
  }
}
