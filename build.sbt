inThisBuild(
  List(
    name := "sbt-scapegoat",
    organization := "com.sksamuel.scapegoat",
    homepage := Some(url("https://github.com/scapegoat-scala/sbt-scapegoat")),
    licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/scapegoat-scala/sbt-scapegoat"),
        "scm:git@github.com:scapegoat-scala/sbt-scapegoat.git",
        Some("scm:git@github.com:scapegoat-scala/sbt-scapegoat.git"),
      ),
    ),
    developers := List(
      Developer(
        "sksamuel",
        "sksamuel",
        "@sksamuel",
        url("https://github.com/sksamuel"),
      ),
    ),
    scalaVersion := "2.12.20",
  ),
)

lazy val root = Project("sbt-scapegoat", file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8"),
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++ Seq("-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false,
    crossScalaVersions += "3.7.3",
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" => (pluginCrossBuild / sbtVersion).value
        case _ => "2.0.0-RC6"
      }
    },
    Test / publishArtifact := false,
    Test / parallelExecution := false,
  )
