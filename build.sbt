name := "sbt-scapegoat"

organization := "com.sksamuel.scapegoat"

version := "0.1.0"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8")

resolvers := ("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2") +: resolvers.value

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

scalaVersion := "2.10.4"

sbtPlugin := true

libraryDependencies ++= Seq(
  "commons-io" % "commons-io" % "2.4"
)

publishTo := Some(Resolver.url("sbt-plugin-releases",
  new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns))

publishMavenStyle := false

publishArtifact in Test := false

parallelExecution in Test := false