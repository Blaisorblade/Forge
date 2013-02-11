package ppl.dsl.forge
package examples

import core.{ForgeApplication,ForgeApplicationRunner}

object IntVectorDSLRunner extends ForgeApplicationRunner with IntVectorDSL

trait IntVectorDSL extends ForgeApplication with ScalaOps {
  /**
   * The name of your DSL. This is the name that will be used in generated files,
   * package declarations, etc.
   */
  def dslName = "IntVector"
    
  /**
   * The specification is the DSL definition (types, data structures, ops, code generators)
   */
  def specification() = {
    /**
     * Include Scala ops
     */
     addScalaOps()
        
    /**
     * Types
     */
    //val T = tpePar("T")
    val IntVector = tpe("IntVector") 
    
    /**
     * Data structures
     */
    data(IntVector, List(), ("_length", MInt), ("_data", tpeInst(GArray(tpePar("T")), List(MInt))))
    
    /* Generic formatting instance */
    val stream = ForgePrinter()
        
    /**
     * Ops
     * 
     * We could simplify this by reusing templates even more, i.e. specializing for different types
     * (e.g. accept a list of binary zip ops that only differentiate in function applied)
     */           
    val vnew = op (IntVector) ("apply", static, List(), List(MInt), IntVector, codegenerated, effect = mutable)
    val vlength = op (IntVector) ("length", infix, List(), List(IntVector), MInt, codegenerated)    
    val vapply = op (IntVector) ("apply", infix, List(), List(IntVector,MInt), MInt, codegenerated)
    val vupdate = op (IntVector) ("update", infix, List(), List(IntVector,MInt,MInt), MUnit, codegenerated, effect = write(0))
    
    //val vtimesScalar = op (IntVector) ("*", infix, List(T withBound TNumeric), List(IntVector,T), IntVector, map((T,T,IntVector), 0, "e => e*"+quotedArg(1)))
    // val vfoo = op (IntVector) ("foo", infix, List(T), List(IntVector,T), tpeInst(IntVector,List(MDouble)), map((T,MDouble,IntVector), 0, "e => 0.0")) // problem: primitive lifting isn't in scope in the ops
      
    val vplus = op (IntVector) ("+", infix, List(), List(IntVector,IntVector), IntVector, zip((MInt,MInt,MInt,IntVector), (0,1), "(a,b) => a+b"))
    
    //val vsum = op (IntVector) ("sum", infix, List(T withBound TNumeric), List(IntVector), T, reduce((T,IntVector), 0, lookup("Numeric","zero").get, "(a,b) => a+b"))
    
    //val vprint = op (IntVector) ("pprint", infix, List(T), List(IntVector), MUnit, foreach((T,IntVector), 0, "a => println(a)")) // will print out of order in parallel, but hey
     
    // val vfilter = op (IntVector) ("filter", infix, List(T), List(IntVector,MFunction(List(T), MBoolean)), IntVector, filter((T,T,IntVector), 0, "e => " + quotedArg(1) + "(e)", "e => e"))
    /*
    val vslice = op (IntVector) ("slice", infix, List(T), List(IntVector, MInt, MInt), IntVector, single(IntVector, { 
      // inside single tasks we use normal DSL code just like applications would (modulo arg names)
      stream.printLines(
        "val st = " + quotedArg(1),
        "val en = " + quotedArg(2),
        "val out = IntVector[T](en - st)",
        "var i = st",        
        "while (i < en) {",
        "  out(i-st) = "+quotedArg(0)+"(i)",
        "  i += 1",
        "}",
        "out"
      )}))        
      */        
    /**
     * DeliteCollectionification
     * This enables a tpe to be passed in as the collection type of a Delite op
     */
    IntVector is DeliteCollection(MInt, vnew, vlength, vapply, vupdate)
    
    /**
     * Code generators
     */
      
    // TODO: how do we refer to other methods or codegenerators inside a particular codegen impl? e.g. vfoo uses vlength   
    codegen (vnew) ($cala, "new "+vnew.tpeName+"("+quotedArg(0)+", new Array[Int]("+quotedArg(0)+"))")
    codegen (vlength) ($cala, quotedArg(0) + "._length")
    codegen (vapply) ($cala, quotedArg(0) + "._data.apply(" + quotedArg(1) + ")")
    codegen (vupdate) ($cala, quotedArg(0) + "._data.update(" + quotedArg(1) + ", " + quotedArg(2) + ")")    
    ()
  }
}
 
