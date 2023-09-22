sbt-scapegoat
=============

![plugin release badge]

sbt-scapegoat is a plugin for SBT that integrates the scapegoat [static code analysis library](http://en.wikipedia.org/wiki/Static_program_analysis). Find out more about scapegoat at the [scapegoat project page](https://github.com/sksamuel/scapegoat).

#### How to use

sbt-scapegoat is an [auto plugin](https://typesafe.com/blog/preview-of-upcoming-sbt-10-features-read-about-the-new-plugins). This means you need SBT 0.13.5 or higher. If you are using an earlier 0.13.x build, you should be able to upgrade to 0.13.5 without any issues.

*Please note*: [scapegoat](https://github.com/sksamuel/scapegoat) only works with Scala 2.11.x, 2.12.x, 2.13.x. There are no plans to release a 2.10.x branch.

*Another note* [scapegoat] Plugin has been migrated to support scala version 2.12.3 and sbt version 1.0.1

Add the plugin to your build with the following in project/plugins.sbt:

```scala
addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat" % "1.2.2") // Verify latest in badge above
```

The plugin has a default Scapegoat version depending on the Scala version you are using.
This version may not always be the latest version of Scapegoat, nor does it always pick a compatible version.

In case you wish to override the version you can do so by setting the `scapegoatVersion` setting in your `build.sbt`.

eg.
```scala
ThisBuild / scapegoatVersion := "2.1.1"
```

![scapegoat release badge]

That's it! You can now generate the scapegoat reports using the `scapegoat`
task:

```
> scapegoat
[info] [scapegoat] setting output dir to [/home/sam/development/workspace/elastic4s/target/scala-2.11/scapegoat-report]
[info] [scapegoat] disabling inspections: ExpressionAsStatement,VarUse
[info] Compiling 47 Scala sources to /home/sam/development/workspace/elastic4s/target/scala-2.11/classes...
[info] [scapegoat]: 55 activated inspections
[info] [scapegoat]: Beginning anaylsis...
[warn] [scapegoat]: Anaylsis complete - 17 errors; 1 warns 2 infos
[info] [scapegoat]: Written HTML report [/home/sam/development/workspace/elastic4s/target/scala-2.11/scapegoat-report/scapegoat.html]
[info] [scapegoat]: Written XML report [/home/sam/development/workspace/elastic4s/target/scala-2.11/scapegoat-report/scapegoat.xml]
```

You should find the reports inside `target/scala-2.11/scapegoat-report`. By default, the reports will be regenerated for all files on every invocation of the `scapegoat` task. If you'd prefer to only have reports generated for files that have changed between invocations, you can set the `scapegoatRunAlways` setting to false. You can then manually force a full inspection by invoking the `scapegoatClean` task, or by doing a full `clean`.


#### Inspections list

The full list of inspections can be seen at the [scapegoat](https://github.com/sksamuel/scapegoat) main page.

#### Console output

sbt-scapegoat generates three sets of output. HTML and XML reports inside `target/scala-2.x/scapegoat-report` and also to the console during the build. The latter is useful so you don't have to open up files to see inspection warnings. However you can disable the console output if needed by setting the following key:

```scala
import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin.autoImport._
scapegoatConsoleOutput := false
```

#### Disabling inspections

Sometimes you might want to disable an inspection, should you disagree with it, or have a need to override it completely.

To do this add the following key to your sbt build, and include the simple name of the inspections you wish to disable. (The full names of inspections are displayed in the XML and HTML reports, in this key you include the simple names).

Eg,

```scala
scapegoatDisabledInspections := Seq("ExpressionAsStatement", "VarUse")
```

#### Ignoring files

Rather that turning off an inspection completely, you might just want to disable scapegoat scanning of a particular file. You can do this with regex matchers on the file path, eg:

```scala
scapegoatIgnoredFiles := Seq(".*/SomeScala.scala")
```

This is a regex that matches on the **full** path of the file, including what directory it happens to be in. For example, the full path might be `/home/sam/development/workspace/scapegoat/scalac-scapegoat-plugin/src/main/scala/com/sksamuel/scapegoat/inspections/VarUse.scala`. To exclude this file, we could use a regex like `.*/VarUse.scala`.

Since this is just regex matching we can do whatever we want - for example we could exclude whole packages, eg `.*/com/sksamuel/.*` or we could exclude whole src trees, eg `.*/src_managed/main/scala/.*`

**Note**: Remember to include the leading `.*/` if you are not matching a path as a literal.

#### Suppressing Warnings by Method or Class

You can suppress a specific warning by method or by class using the [`java.lang.SuppressWarnings`](http://docs.oracle.com/javase/7/docs/api/java/lang/SuppressWarnings.html) anotation.

Use the simple name of the inspection to be ignored as the argument, or use `"all"` to suppress all `scapegoat` warnings in the specified scope.

Some examples:
```scala
@SuppressWarnings(Array("all"))
class Test {
  def hello : Unit = {
    val s : Any = "sammy"
    println(s.asInstanceOf[String])
  }
} 

class Test2 {
  @SuppressWarnings(Array("AsInstanceOf"))
  def hello : Unit = {
    val s : Any = "sammy"
    println(s.asInstanceOf[String])
  }
} 
```

#### Cross build for Scala 2.11

```scala
scapegoatVersion := {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 11)) => "1.4.9"
    case _ => "2.1.1"
  }
}
```

#### Inspection warning level overrides

If you want to change the warning level of an inspection, for example to downgrade "TraversableHead" and "OptionGet" from Errors to Warnings, add the following to your build.sbt:

```scala
scalacOptions in Scapegoat += "-P:scapegoat:overrideLevels:TraversableHead=Warning:OptionGet=Warning",
```

The string should be a colon separated list of name=level settings, where 'name' is the simple name of an inspection and 'level' is the simple name of a com.sksamuel.scapegoat.Level constant, e.g. 'Warning'.

#### False positives

Please note that scapegoat is a new project. While it's been tested on some common open source projects, there is still a good chance you'll find false positives. Please open up issues if you run into these so we can fix them.

#### Reporting Issues

If you have an error please do report it, but it would be helpful if the issues were logged at the [scapegoat](https://github.com/sksamuel/scapegoat) issues page, unless the bug is directly related to the plugin. Eg, inspection false positives or false negatives are errors in the scalac compiler plugin, not the sbt plugin.


[plugin release badge]: https://img.shields.io/github/v/tag/scapegoat-scala/sbt-scapegoat?label=Latest%20Release
[plugin release link]: https://maven-badges.herokuapp.com/maven-central/com.sksamuel.scapegoat/sbt-scapegoat
[scapegoat release badge]: https://maven-badges.herokuapp.com/maven-central/com.sksamuel.scapegoat/scalac-scapegoat-plugin_2.13.6/badge.svg?subject=Latest%20scapegoat&color=yellowgreen
