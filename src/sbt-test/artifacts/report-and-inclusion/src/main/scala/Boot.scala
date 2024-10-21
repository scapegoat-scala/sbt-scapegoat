import cats.syntax.all._

object Boot extends App {

  // Depend on Cats to verify that dependencies are maintained
  println("Hello " |+| " World")

  val ohno = Option(42).get

}
