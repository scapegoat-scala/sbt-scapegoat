package my.inspections

import com.sksamuel.scapegoat.test.ScapegoatTestPluginRunner
import org.scalatest.{OneInstancePerTest, Matchers, FreeSpec}

class DisallowJavaDateConstructionSpec
  extends FreeSpec
  with Matchers
  with ScapegoatTestPluginRunner
  with OneInstancePerTest {

  override val inspections = Seq(new DisallowJavaDateConstruction)

  "DisallowJavaDateConstruction" - {
    "should report warning when java.util.Date constructed" in {

      val code =
        """import java.util.Date
          |
          |class Test {
          |  println(new Date())
          |}
        """.stripMargin

      compileCodeSnippet(code)
      compiler.scapegoat.feedback.warnings.size shouldBe 1
    }

    "should not report warning when other Date constructed" in {

      val code =
        """class Date {}
          |
          |class Test {
          |  println(new Date())
          |}
        """.stripMargin

      compileCodeSnippet(code)
      compiler.scapegoat.feedback.warnings.size shouldBe 0
    }
  }
}
