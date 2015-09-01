name := "sbt-scapegoat"

organization := "com.sksamuel.scapegoat"

version := "1.1.0"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8")

resolvers := ("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2") +: resolvers.value

javacOptions ++= Seq("-source", "1.6", "-target", "1.6")

scalaVersion := "2.10.5"

sbtPlugin := true

publishTo := Some(Resolver.url("sbt-plugin-releases",
  new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns))

publishMavenStyle := false

publishArtifact in Test := false

parallelExecution in Test := false

ScriptedPlugin.scriptedSettings

scriptedLaunchOpts ++= Seq(
  "-Xmx1024M", "-XX:MaxPermSize=256M",
  "-Dplugin.version=" + version.value
)

/**
 * Before the "scripted" SBT tests are run, we "publishLocal" the example
 * inspections, so that the "custom-inspection-in-external-project" test can
 * depend on them via Ivy.
 */
val publishExampleInspections = sbt.TaskKey[Unit]("publishExampleInspections")

publishExampleInspections := {
  val sbt = if (System.getProperty("os.name").startsWith("Windows"))
    List("cmd", "/c", "sbt")
  else
    List("sbt")

  val cmd = sbt ++ List("-Dplugin.version=" + version.value, "publishLocal")

  println("[info] Running: " + cmd.mkString(" "))

  val ret = Process(
    cmd,
    new File("src/sbt-test/scapegoat/custom-inspection-in-subproject"))
    .run()
    .exitValue()

  require(ret == 0)
}

scripted <<= scripted.dependsOn(publishExampleInspections)
