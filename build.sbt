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
    scalaVersion := "2.12.21",
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
    crossScalaVersions := Seq("2.12.21", "3.9.0"),
    scalacOptions ++= {
      if (scalaBinaryVersion.value == "2.12") Seq("-Xsource:3", "-release:8") else Nil
    },
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.11.7"
        case _ => "2.1.0"
      }
    },
    scriptedSbt := (if (scalaBinaryVersion.value == "2.12") "1.13.1" else "2.1.0"),
    Test / publishArtifact := false,
    Test / parallelExecution := false,
  )
