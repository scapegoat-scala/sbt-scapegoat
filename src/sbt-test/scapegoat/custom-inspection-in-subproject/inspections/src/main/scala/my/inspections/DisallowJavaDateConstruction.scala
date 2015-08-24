package my.inspections

import com.sksamuel.scapegoat._

/**
 * A compile-time [[Inspection]] which detects usage of "new java.util.Date()"
 * as an example.
 */
class DisallowJavaDateConstruction extends Inspection {

  def inspector(context: InspectionContext): Inspector = new Inspector(context) {

    import context.global._

    override def postTyperTraverser = Some(new context.Traverser {

      override def inspect(tree: Tree): Unit = tree match {

        case New(clazz) if clazz.toString() == "java.util.Date" =>
          context.warn(
            "Use of new java.util.Date() is disallowed",
            tree.pos,
            Levels.Error,
            "In this project, you may not directly construct a new Date(). Instead, please " +
              "use our hypothetical super test framework which lets you inject a mock time. " +
              "(Don't get too hung up on this inpsection; it's just an example!)",
            DisallowJavaDateConstruction.this)

        case _ => continue(tree)
      }
    })
  }
}
