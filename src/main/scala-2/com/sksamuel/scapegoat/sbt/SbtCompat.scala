package com.sksamuel.scapegoat.sbt

object SbtCompat {
  implicit class DefCompat(private val self: sbt.Def.type) extends AnyVal {
    def uncached[A](a: A): A = a
  }
}
