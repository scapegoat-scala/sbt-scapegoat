import models.ImportClass

object Boot extends App {

  println("Hello World")
  val ohno = Option(ImportClass(42)).get

}
