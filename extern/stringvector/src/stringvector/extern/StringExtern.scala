package stringvector.extern

import scala.annotation.unchecked.uncheckedVariance
import scala.reflect.{Manifest,SourceContext}
import scala.virtualization.lms.common._

import ppl.delite.framework.codegen.delite.overrides._
import ppl.delite.framework.ops.DeliteOpsExp

/**
 *  A wrapped String class to use in Forge
 */
/*
implicit class TString(s: String) {
  def infix_+(o: TString): TString = new TString(this.s + o.s)
  def infix_+(o: String): TString = new TString(this.s + o)
}
*/

trait WString[A] extends BaseWrapper {
  def +(a: Rep[A], b: Rep[A]): Rep[A]
}

implicit def WStringFoo = new WString[String] {
  def +(a: Rep[String], y: Rep[String]) = a + y;
}
