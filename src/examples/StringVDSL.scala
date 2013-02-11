package ppl.dsl.forge
package examples

import core.{ForgeApplication,ForgeApplicationRunner}

object StringVDSLRunner extends ForgeApplicationRunner with StringVDSL

trait StringVDSL extends ForgeApplication with ScalaOps {
  /**
   * The name of your DSL. This is the name that will be used in generated files,
   * package declarations, etc.
   */
  def dslName = "StringV"
    
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
    val StringV = tpe("StringV") 
    
    /**
     * Data structures
     */
    data(StringV, List(), ("_length", MInt), ("_data", tpeInst(GArray(tpePar("T")), List(MAS))))
    
    /* Generic formatting instance */
    val stream = ForgePrinter()
        
    /**
     * Ops
     * 
     * We could simplify this by reusing templates even more, i.e. specializing for different types
     * (e.g. accept a list of binary zip ops that only differentiate in function applied)
     */           
    val vnew = op (StringV) ("apply", static, List(), List(MInt), StringV, codegenerated, effect = mutable)
    val vlength = op (StringV) ("length", infix, List(), List(StringV), MInt, codegenerated)    
    val vapply = op (StringV) ("apply", infix, List(), List(StringV,MInt), MAS, codegenerated)
    val vupdate = op (StringV) ("update", infix, List(), List(StringV,MInt,MAS), MUnit, codegenerated, effect = write(0))
    
    //val vplus = op (StringV) ("+", infix, List(), List(StringV,StringV), StringV, zip((MAS,MAS,MAS,StringV), (0,1), "(a,b) => a+b"))
    
    /**
     * DeliteCollectionification
     * This enables a tpe to be passed in as the collection type of a Delite op
     */
    StringV is DeliteCollection(MAS, vnew, vlength, vapply, vupdate)
    
    /**
     * Code generators
     */
      
    // TODO: how do we refer to other methods or codegenerators inside a particular codegen impl? e.g. vfoo uses vlength   
    codegen (vnew) ($cala, "new "+vnew.tpeName+"("+quotedArg(0)+", new Array[Array[String]]("+quotedArg(0)+"))")
    codegen (vlength) ($cala, quotedArg(0) + "._length")
    codegen (vapply) ($cala, quotedArg(0) + "._data.apply(" + quotedArg(1) + ")")
    codegen (vupdate) ($cala, quotedArg(0) + "._data.update(" + quotedArg(1) + ", " + quotedArg(2) + ")")    
    ()
  }
}
 
