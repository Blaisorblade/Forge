package ppl.dsl.forge
package core

trait Definitions {
  this: Forge =>

  /**
   * String constants
   */
  val opIdentifierPrefix = "mn"
  val opArgPrefix = "__arg"
  val implicitOpArgPrefix = "__imp"
  val qu = "__quote"
  
  /**
   * Built-in types
   */  
  
  // concrete types (M stands for "Meta")
  lazy val MAny = tpe("Any")
  lazy val MInt = tpe("Int")
  lazy val MDouble = tpe("Double")
  lazy val MBoolean = tpe("Boolean")
  lazy val MString = tpe("String")
  lazy val MUnit = tpe("Unit")
  lazy val byName = tpe("Thunk")
  def MThunk(ret: Rep[DSLType]) = ftpe(List(byName), ret)
  def MFunction(args: List[Rep[DSLType]], ret: Rep[DSLType]) = ftpe(args,ret)  
  lazy val MSourceContext = tpe("SourceContext")
  
  // generic types
  // should these return a different Forge type (e.g. Rep[TypeConstructor] or Rep[GenericType]) than concrete types?
  def GVar(tpePar: Rep[TypePar]) = tpe("Var", List(tpePar)) 
  def GArray(tpePar: Rep[TypePar]) = tpe("Array", List(tpePar))     
  
  /**
   * stage tags - only 2 stages
   */
  object future extends StageTag
  object now extends StageTag
  
  /**
   * code generators
   */  
  object $cala extends CodeGenerator { def name = "Scala" }  // odd things happen if you try to re-use the existing object name 'scala'
  object cuda extends CodeGenerator { def name = "Cuda" }  
  object opencl extends CodeGenerator { def name = "OpenCL" }  
  object c extends CodeGenerator { def name = "C" }  
  
  val generators = List($cala, cuda, opencl, c)
  
  /**
   * Type classes
   * DSLs can extend these by adding their own
   */
  object TManifest extends TypeClass {
    def name = "Manifest"
    def prefix = "_m"
  }
  object TNumeric extends TypeClass {
    def name = "Numeric"
    def prefix = "_num"
  }
  object TOrdering extends TypeClass {
    def name = "Ordering"
    def prefix = "_ord"
  }

  
  /**
   * Method syntax types
   */
  object static extends MethodType
  object infix extends MethodType
  object direct extends MethodType
  
  // blacklist for op names that cannot be expressed with infix methods
  val noInfixList = List("apply", "update") 
  
  /**
   * Effect types
   */  
  object pure extends EffectType
  object mutable extends EffectType  
  object simple extends EffectType
  case class write(args: Int*) extends EffectType
  
  /**
   * Delite op types
   */
  object codegenerated extends OpType
  
  abstract class DeliteOpType extends OpType

  /**
   * SingleTask
   * 
   * @param retTpe    R, the return type of the function
   * @param func      string representation of the function ( => R)
   */
  def forge_single(retTpe: Rep[DSLType], func: Rep[String]): DeliteOpType
  object single {
    def apply(retTpe: Rep[DSLType], func: Rep[String]) = forge_single(retTpe, func)
  }
  
  /**
   * Map
   * 
   * @param tpePars   [A,R,C[R]]
   * @param argIndex  index of op argument that correspond to map argument in (collection to be mapped)
   * @param func      string representation of a map function a: A => R
   */
   def forge_map(tpePars: (Rep[DSLType],Rep[DSLType],Rep[DSLType]), argIndex: Int, func: String): DeliteOpType
   object map {
     def apply(tpePars: (Rep[DSLType],Rep[DSLType],Rep[DSLType]), mapArgIndex: Int, func: String) = forge_map(tpePars, mapArgIndex, func)
   }  
   
  /**
   * ZipWith
   * 
   * @param tpePars       [A,B,R,C[R]]
   * @param argIndices    index of op arguments that correspond to zip arguments inA, inB (first and second collection respectively)
   * @param func          string representation of a zip function (a: A, b: B) => R
   */
  def forge_zip(tpePars: (Rep[DSLType],Rep[DSLType],Rep[DSLType],Rep[DSLType]), argIndices: (Int,Int), func: String): DeliteOpType
  object zip {
    // def apply[T](x: (T,T) => T)
    def apply(tpePars: (Rep[DSLType],Rep[DSLType],Rep[DSLType],Rep[DSLType]), zipArgIndices: (Int,Int), func: String) = forge_zip(tpePars, zipArgIndices, func)
  }
  
  /**
   * Filter
   */
  
  /**
   * Foreach
   */                             
}


trait DefinitionsExp extends Definitions with DerivativeTypes {
  this: ForgeExp =>
  
  case class SingleTask(retTpe: Rep[DSLType], func: Rep[String]) extends DeliteOpType
  def forge_single(retTpe: Rep[DSLType], func: Rep[String]) = SingleTask(retTpe, func)
  
  case class Map(tpePars: (Rep[DSLType],Rep[DSLType],Rep[DSLType]), argIndex: Int, func: String) extends DeliteOpType  
  def forge_map(tpePars: (Rep[DSLType],Rep[DSLType],Rep[DSLType]), argIndex: Int, func: String) = Map(tpePars, argIndex, func)
    
  case class Zip(tpePars: (Rep[DSLType],Rep[DSLType],Rep[DSLType],Rep[DSLType]), argIndices: (Int,Int), func: String) extends DeliteOpType  
  def forge_zip(tpePars: (Rep[DSLType],Rep[DSLType],Rep[DSLType],Rep[DSLType]), argIndices: (Int,Int), func: String) = Zip(tpePars, argIndices, func)
}