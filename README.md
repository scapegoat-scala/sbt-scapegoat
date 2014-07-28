sbt-scapegoat
=============

sbt-scapegoat is a plugin for SBT that integrates the scapegoat static analysis library. Find out more about [scapegoat](https://github.com/sksamuel/scapegoat).

#### How to use

sbt-scapegoat is an auto plugin. This means you need SBT 0.13.5 or higher.

Add the plugin to your build with the following in project/plugins.sbt:

```addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat" % "0.4.0")```

Then when SBT compiles your code, the scapegoat reports will be generated. You should find these inside target/scala-2.11/scapegoat-report.
