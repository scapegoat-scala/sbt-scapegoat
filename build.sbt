name := "sbt-scapegoat"
organization := "com.sksamuel.scapegoat"
homepage := Some(url("https://github.com/scapegoat-scala/sbt-scapegoat"))
licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/scapegoat-scala/sbt-scapegoat"),
    "scm:git@github.com:scapegoat-scala/sbt-scapegoat.git",
    Some("scm:git@github.com:scapegoat-scala/sbt-scapegoat.git"),
  ),
)
developers := List(
  Developer(
    "sksamuel",
    "sksamuel",
    "@sksamuel",
    url("https://github.com/sksamuel"),
  ),
)

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8")
javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

// Align Scala (major) version and SBT versions
// This avoids issues from running commands without ^ prefix for cross compiling plugins
scalaVersion := "2.12.17"

sbtPlugin := true
enablePlugins(SbtPlugin)
scriptedLaunchOpts := {
  scriptedLaunchOpts.value ++ Seq("-Dplugin.version=" + version.value)
}
scriptedBufferLog := false
crossSbtVersions := Seq("1.5.8")

Test / publishArtifact := false
Test / parallelExecution := false
