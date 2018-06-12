name := "sbt-scapegoat"

organization := "com.sksamuel.scapegoat"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8")

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

scalaVersion := "2.12.6"

sbtPlugin := true

publishTo := {
    val isSnapshotValue = isSnapshot.value
    val nexus = "https://oss.sonatype.org/"
    if(isSnapshotValue) Some("snapshots" at nexus + "content/repositories/snapshots")
    else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

parallelExecution in Test := false

sbtrelease.ReleasePlugin.autoImport.releasePublishArtifactsAction := PgpKeys.publishSigned.value

sbtrelease.ReleasePlugin.autoImport.releaseCrossBuild := false

pgpSecretRing := file("/home/sam/Downloads/gpg.private")

pgpPublicRing := file("/home/sam/Downloads/gpg.public")

pomIncludeRepository := {
  _ => false
}

pomExtra := {
  <url>https://github.com/sksamuel/sbt-scapegoat</url>
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:sksamuel/sbt-scapegoat.git</url>
      <connection>scm:git@github.com:sksamuel/sbt-scapegoat.git</connection>
    </scm>
    <developers>
      <developer>
        <id>sksamuel</id>
        <name>sksamuel</name>
        <url>http://github.com/sksamuel</url>
      </developer>
    </developers>
}
