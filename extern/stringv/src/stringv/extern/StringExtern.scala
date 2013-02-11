package stringv.extern

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
/*
trait CanAddOps extends Variables {
  
  trait CanAdd[A] {
    def +(a: Rep[A], b: Rep[A]): Rep[A]
  }

  //implicit def canAddView[A:Manifest] = new CanAdd[A] {
  //  def +(a: Rep[A], b: Rep[A]): Rep[A] = a+b
  //}

  implicit def canAddView[String] : CanAdd[String] = new CanAdd[String] {
    def +(a: Rep[String], b: Rep[String]): Rep[String] = {return a.+(b)}
  }
}
*/
/*
object StringWrapper {
  implicit def CanAddFoo = new CanAdd[String] {
    def +(a: Rep[String], b: Rep[String]) = a + b;
    //def +(a: Rep[String], b: Rep[String]) = unit(a + b);
  }
}
*/
