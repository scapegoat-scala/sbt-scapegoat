sbt-scapegoat
=============

sbt-scapegoat is a plugin for SBT that integrates the scapegoat [static code analysis library](http://en.wikipedia.org/wiki/Static_program_analysis). Find out more about scapegoat at the [scapegoat project page](https://github.com/sksamuel/scapegoat).

[![Join the chat at https://gitter.im/sksamuel/scalac-scapegoat-plugin](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/sksamuel/scalac-scapegoat-plugin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

#### How to use

sbt-scapegoat is an [auto plugin](https://typesafe.com/blog/preview-of-upcoming-sbt-10-features-read-about-the-new-plugins). This means you need SBT 0.13.5 or higher. If you are using an earlier 0.13.x build, you should be able to upgrade to 0.13.5 without any issues.

*Please note*: [scapegoat](https://github.com/sksamuel/scapegoat) only works with Scala 2.11.x. There are no plans to release a 2.10.x branch.

Add the plugin to your build with the following in project/plugins.sbt:

```scala
addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat" % "1.1.0")
```

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

You should find the reports inside `target/scala-2.11/scapegoat-report`.

#### Inspections list

The full list of inspections can be seen at the [scapegoat](https://github.com/sksamuel/scapegoat) main page.

#### Console output

sbt-scapegoat generates three sets of output. HTML and XML reports inside `target/scala-2.x/scapegoat-report` and also to the console during the build. The latter is useful so you don't have to open up files to see inspection warnings. However you can disable the console output if needed by setting the following key:

`import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin.autoImport._`
`scapegoatConsoleOutput := false`

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
```
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

#### Writing custom inspections

Scapegoat supports custom inspections, for example to enforce a
project-specific rule.

How to write and test the inspection class is out of scope of this Readme, but
you can take a look at the source code for
[the inspections which are included in the scalac-scapegoat-plugin project](https://github.com/sksamuel/scalac-scapegoat-plugin/tree/master/src/main/scala/com/sksamuel/scapegoat/inspections).

The `scapegoatCustomInspections` SBT SettingKey needs to list all the custom inspections by their fully-qualified class name. For example, add something like the following to your `build.sbt`:

    scapegoatCustomInspections := List(
      "my.inspections.InspectionOne",
      "my.inspections.InspectionOne")

The `scapegoatCustomInspectionsClasspath` SBT SettingKey needs to list the classpath entries at which your inspection classes can be loaded. If your inspections have come from a compiled JAR, then you can list its file location here. (There is not currently automatic support for using an Ivy-provided JAR dependency here. You may be able to do this by accessing the "update" SBT TaskKey. There is some code along these lines in [sbt-scapegoat](https://github.com/sksamuel/sbt-scapegoat/blob/ae4231d1341eeece323e111c757d57d904e66f7b/src/main/scala/com/sksamuel/scapegoat/sbt/ScapegoatSbtPlugin.scala#L41) which you can use for inspiration.)

    scapegoatCustomInspectionsClasspath := List(new File("lib/my-inpections-1.0.jar"))

If you would like to have the inspection classes in the same project, you can add them to the SBT `project/` build. Put your inspection classes under `project` (e.g. `project/src/main/scala/my/inspections/MyInspection.scala`) and use the following for the `scapegoatCustomInspectionsClasspath`:


      scapegoatCustomInspectionsClasspath := List({
        // Get the classpath dir of the SBT build to allow loading the inspections.
        // There might be a better way to get this from SBT, but I can't find it.
        // You can always hardcode "./project/target/scala-2.10/sbt-0.13/classes/" if you don't
        // like this.
        classOf[my.inspections.MyInspection].getClassLoader match {
          case urlLoader: java.net.URLClassLoader => urlLoader.getURLs.toList match {
            case List(u) if u.getProtocol == "file" =>
              new File(u.toURI)
          }
        }
      }),

#### False positives

Please note that scapegoat is a new project. While it's been tested on some common open source projects, there is still a good chance you'll find false positives. Please open up issues if you run into these so we can fix them.

#### Reporting Issues

If you have an error please do report it, but it would be helpful if the issues were logged at the [scapegoat](https://github.com/sksamuel/scapegoat) issues page, unless the bug is directly related to the plugin. Eg, inspection false positives or false negatives are errors in the scalac compiler plugin, not the sbt plugin.
