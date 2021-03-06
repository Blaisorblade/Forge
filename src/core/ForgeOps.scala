package ppl.dsl.forge
package core

import java.io.{PrintWriter}
import scala.reflect.SourceContext
import scala.virtualization.lms.common._
import scala.virtualization.lms.internal._
import scala.collection.mutable.{ArrayBuffer,HashMap}

trait ForgeOps extends Base {
  this: Forge =>
    
  def grp(name: String) = forge_grp(name)  
  def tpePar(name: String, ctxBounds: List[TypeClass] = List()) = forge_tpepar(name, ctxBounds) // TODO: type bounds
  def tpe(name: String, tpePars: List[Rep[TypePar]] = List(), stage: StageTag = future) = forge_tpe(name, tpePars, stage)
  def tpeInst(hkTpe: Rep[DSLType], tpeArgs: List[Rep[DSLType]] = List(), stage: StageTag = future) = forge_tpeinst(hkTpe, tpeArgs, stage)
  def ftpe(args: List[Rep[DSLType]], ret: Rep[DSLType]) = forge_ftpe(args, ret)
  def lift(grp: Rep[DSLGroup])(tpe: Rep[DSLType]) = forge_lift(grp, tpe)
  def data(tpe: Rep[DSLType], tpePars: List[Rep[TypePar]], fields: (String, Rep[DSLType])*) = forge_data(tpe, tpePars, fields)
  def op(grp: Rep[DSLGroup])(name: String, style: MethodType, tpePars: List[Rep[TypePar]], args: List[Rep[DSLType]], retTpe: Rep[DSLType], opTpe: OpType, effect: EffectType = pure, implicitArgs: List[Rep[DSLType]] = List(MSourceContext)) = forge_op(grp,name,style,tpePars,args,implicitArgs,retTpe,opTpe,effect)
  def codegen(op: Rep[DSLOp])(generator: CodeGenerator, rule: Rep[String]) = forge_codegen(op,generator,rule)
  def extern(grp: Rep[DSLGroup], withLift: Boolean = false, targets: List[CodeGenerator] = generators) = forge_extern(grp, withLift, targets)
  
  def infix_withBound(a: Rep[TypePar], b: TypeClass) = forge_withBound(a,b)
    
  def infix_is(tpe: Rep[DSLType], dc: DeliteCollection) = forge_isdelitecollection(tpe, dc)  
  case class DeliteCollection(val tpeArg: Rep[DSLType], val alloc: Rep[DSLOp], val size: Rep[DSLOp], val apply: Rep[DSLOp], val update: Rep[DSLOp])

  def lookup(grpName: String, opName: String): Option[Rep[DSLOp]]
  
  def forge_grp(name: String): Rep[DSLGroup]  
  def forge_tpepar(name: String, ctxBounds: List[TypeClass]): Rep[TypePar]
  def forge_tpe(name: String, tpePars: List[Rep[TypePar]], stage: StageTag): Rep[DSLType]    
  def forge_tpeinst(hkTpe: Rep[DSLType], tpeArgs: List[Rep[DSLType]], stage: StageTag): Rep[DSLType]    
  def forge_ftpe(args: List[Rep[DSLType]], ret: Rep[DSLType]): Rep[DSLType]
  def forge_lift(grp: Rep[DSLGroup], tpe: Rep[DSLType]): Rep[Unit]
  def forge_data(tpe: Rep[DSLType], tpePars: List[Rep[TypePar]], fields: Seq[(String, Rep[DSLType])]): Rep[DSLData]  
  def forge_op(tpe: Rep[DSLGroup], name: String, style: MethodType, tpePars: List[Rep[TypePar]], args: List[Rep[DSLType]], implicitArgs: List[Rep[DSLType]], retTpe: Rep[DSLType], opTpe: OpType, effect: EffectType): Rep[DSLOp]
  def forge_codegen(op: Rep[DSLOp], generator: CodeGenerator, rule: Rep[String]): Rep[CodeGenRule]
  def forge_extern(grp: Rep[DSLGroup], withLift: Boolean, targets: List[CodeGenerator]): Rep[Unit]
  
  def forge_withBound(a: Rep[TypePar], b: TypeClass): Rep[TypePar]
  def forge_isdelitecollection(tpe: Rep[DSLType], dc: DeliteCollection): Rep[Unit]
}

trait ForgeOpsExp extends ForgeOps with BaseExp {
  this: ForgeExp  =>

  /*
   * Compiler state
   */
  val Lifts = HashMap[Exp[DSLGroup],ArrayBuffer[Exp[DSLType]]]()
  val Tpes = ArrayBuffer[Exp[DSLType]]()
  val DataStructs = ArrayBuffer[Exp[DSLData]]()
  val OpsGrp = HashMap[Exp[DSLGroup],DSLOps]()  
  val CodeGenRules = HashMap[Exp[DSLGroup],ArrayBuffer[Exp[CodeGenRule]]]()
  val DeliteCollections = HashMap[Exp[DSLType], DeliteCollection]()
  val Externs = ArrayBuffer[Extern]()
  
  /**
   * Convenience method providing access to defined ops in other modules
   */  
  def lookup(grpName: String, opName: String): Option[Rep[DSLOp]] = {
    OpsGrp.find(t => t._1.name == grpName).flatMap(_._2.ops.find(_.name == opName))
  }
       
  /**
   * IR Definitions
   */
  
  /* A group represents a collection of ops which become an op trait in the generated DSL */
  case class Grp(name: String) extends Def[DSLGroup]
  
  def forge_grp(name: String) = Grp(name)
  
  /* TpePar represents a named type parameter for another DSLType */
  // no higher-kinded fun yet
  case class TpePar(name: String, ctxBounds: List[TypeClass]) extends Def[TypePar]
   
  def forge_tpepar(name: String, ctxBounds: List[TypeClass]) = TpePar(name, ctxBounds)
  
  /* Adds a bound to a type parameter by constructing a new type parameter */
  def forge_withBound(a: Rep[TypePar], b: TypeClass) = tpePar(a.name, b :: a.ctxBounds)
   
  /* A DSLType */    
  case class Tpe(name: String, tpePars: List[Rep[TypePar]], stage: StageTag) extends Def[DSLType]
  
  def forge_tpe(name: String, tpePars: List[Rep[TypePar]], stage: StageTag) = {
    val t = Tpe(name, tpePars, stage)
    Tpes += t
    t
  }
  
  /* A DSLType instance of a higher-kinded type */
  case class TpeInst(hkTpe: Rep[DSLType], tpeArgs: List[Rep[DSLType]], stage: StageTag) extends Def[DSLType]
    
  def forge_tpeinst(hkTpe: Rep[DSLType], tpeArgs: List[Rep[DSLType]], stage: StageTag) = {
    if (tpeArgs.length != hkTpe.tpePars.length) err("cannot instantiate tpe " + hkTpe.name + " with args " + tpeArgs + " - not enough arguments")
    val t = TpeInst(hkTpe, tpeArgs, stage)
    Tpes += t
    t
  }
    
  /* A function of Rep arguments */
  case class FTpe(args: List[Rep[DSLType]], ret: Rep[DSLType]) extends Def[DSLType]
  
  def forge_ftpe(args: List[Rep[DSLType]], ret: Rep[DSLType]) = FTpe(args,ret)
  
  /* A statement declaring a type to lift in a particular scope (group) */
  def forge_lift(grp: Rep[DSLGroup], tpe: Rep[DSLType]) = {
    val buf = Lifts.getOrElseUpdate(grp, new ArrayBuffer[Exp[DSLType]]())
    buf += tpe
    ()
  }
    
  /* A back-end data structure */
  case class Data(tpe: Rep[DSLType], tpePars: List[Rep[TypePar]], fields: Seq[(String, Exp[DSLType])]) extends Def[DSLData]
  
  def forge_data(tpe: Rep[DSLType], tpePars: List[Rep[TypePar]], fields: Seq[(String, Exp[DSLType])]) = {
    val d = Data(tpe, tpePars, fields)
    DataStructs += d
    d
  }
  
  /* An operator - this represents a method or IR node */
  case class Op(grp: Rep[DSLGroup], name: String, style: MethodType, tpePars: List[Rep[TypePar]], args: List[Rep[DSLType]], implicitArgs: List[Rep[DSLType]], retTpe: Rep[DSLType], opTpe: OpType, effect: EffectType) extends Def[DSLOp]
  
  def forge_op(_grp: Rep[DSLGroup], name: String, style: MethodType, tpePars: List[Rep[TypePar]], args: List[Rep[DSLType]], implicitArgs: List[Rep[DSLType]], retTpe: Rep[DSLType], opTpe: OpType, effect: EffectType) = {
    val o = Op(_grp, name, style, tpePars, args, implicitArgs, retTpe, opTpe, effect)
    val opsGrp = OpsGrp.getOrElseUpdate(_grp, new DSLOps {
      val grp = _grp
      override def targets: List[CodeGenerator] = if (opTpe == codegenerated) List($cala) else List() // TODO 
    })
    opsGrp.ops ::= o
    o
  }
  
  /* A code gen rule - this is the imperative code defining how to implement a particular op */
  case class CodeGenDecl(op: Rep[DSLOp], generator: CodeGenerator, rule: Rep[String], isSimple: Boolean) extends Def[CodeGenRule]
  
  def forge_codegen(op: Rep[DSLOp], generator: CodeGenerator, rule: Rep[String]) = {
    val isSimple = rule match {
      case Def(PrintLines(x, l)) => false
      case _ => true
    }
    val c = CodeGenDecl(op, generator, rule, isSimple)
    if (CodeGenRules.get(op.grp).exists(_.exists(_.op == op))) err("multiple code generators declared for op " + op.grp + op.name)
    
    val buf = CodeGenRules.getOrElseUpdate(op.grp, new ArrayBuffer[Exp[CodeGenRule]]())
    buf += c
    c
  }
    
  /* Establishes that the given tpe implements the DeliteCollection interface */
  def forge_isdelitecollection(tpe: Rep[DSLType], dc: DeliteCollection) = {
    // verify the dc functions match our expectations
    if ((dc.alloc.args != List(MInt) || dc.alloc.retTpe != tpe))
      // TODO: how should this work? alloc can really map to anything.. e.g can have other fields that get mapped from the inputs in arbitrary ways
      // needs to be specified in the zip in some context attached to the alloc method?      
      err("dcAlloc must take a single argument of type " + MInt.name + " and return an instance of " + tpe.name)
    if ((dc.size.args != List(tpe)) || (dc.size.retTpe != MInt))
      err("dcSize must take a single argument of type " + tpe.name + " and return an MInt")
    if ((dc.apply.args != List(tpe, MInt)) || (dc.apply.retTpe != dc.tpeArg))
      err("dcApply must take two arguments of type(" + tpe.name + ", " + MInt.name + ") and return a " + dc.tpeArg.name)
    if ((dc.update.args != List(tpe, MInt, dc.tpeArg)) || (dc.update.retTpe != MUnit))
      err("dcUpdate must take arguments of type (" + tpe.name + ", " + MInt.name + ", " + dc.tpeArg.name + ") and return " + MUnit.name)
    
    DeliteCollections += (tpe -> dc)
    ()
  }
  
  /* A reference to an external ops group */
  case class Extern(opsGrp: DSLOps, withLift: Boolean) extends Def[Unit]
  
  def forge_extern(_grp: Rep[DSLGroup], withLift: Boolean, _targets: List[CodeGenerator]) = {
    val opsGrp = new DSLOps {
      val grp = _grp
      override def targets = _targets
    }
    val e = Extern(opsGrp, withLift)
    if (!Externs.contains(e)) Externs += e
    e
  }
}


trait ScalaGenForgeOps extends ScalaGenBase {
  val IR: ForgeOpsExp
  import IR._

  override def emitNode(sym: Sym[Any], rhs: Def[Any]) = rhs match {
    case _ => super.emitNode(sym, rhs)
  }
}