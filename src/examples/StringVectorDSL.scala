package ppl.dsl.forge
package examples

import core.{ForgeApplication,ForgeApplicationRunner}

object StringVectorDSLRunner extends ForgeApplicationRunner with StringVectorDSL

trait StringVectorDSL extends ForgeApplication with ScalaOps {
  /**
   * The name of your DSL. This is the name that will be used in generated files,
   * package declarations, etc.
   */
  def dslName = "StringVector"
    
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
    val T = tpePar("T")
    val StringVector = tpe("StringVector", List(T)) 
    
    /**
     * Data structures
     */
    data(StringVector, List(T), ("_length", MInt), ("_data", GArray(T)))
    
    /* Generic formatting instance */
    val stream = ForgePrinter()
        
    /**
     * Ops
     */
 
    val vnew = op (StringVector) ("apply", static, List(T), List(MInt), StringVector, codegenerated, effect = mutable)
    val vlength = op (StringVector) ("length", infix, List(T), List(StringVector), MInt, codegenerated)    
    val vapply = op (StringVector) ("apply", infix, List(T), List(StringVector,MInt), T, codegenerated)
    val vupdate = op (StringVector) ("update", infix, List(T), List(StringVector,MInt,T), MUnit, codegenerated, effect = write(0))
    
    val vplus = op (StringVector) ("+", infix, List(T withBound TString), List(StringVector, StringVector), StringVector, zip((T,T,T,StringVector), (0,1), "(a,b) => a+b"))
     
    /**
     * DeliteCollectionification
     * This enables a tpe to be passed in as the collection type of a Delite op
     */
    StringVector is DeliteCollection(T, vnew, vlength, vapply, vupdate)
    
    /**
     * Code generators
     */
      
    // TODO: how do we refer to other methods or codegenerators inside a particular codegen impl? e.g. vfoo uses vlength   
    codegen (vnew) ($cala, "new "+vnew.tpeName+"["+vnew.tpeInstance(0)+"]("+quotedArg(0)+", new Array["+vnew.tpeInstance(0)+"]("+quotedArg(0)+"))")
    codegen (vlength) ($cala, quotedArg(0) + "._length")
    codegen (vapply) ($cala, quotedArg(0) + "._data.apply(" + quotedArg(1) + ")")
    codegen (vupdate) ($cala, quotedArg(0) + "._data.update(" + quotedArg(1) + ", " + quotedArg(2) + ")")    
    ()
  }
}
 
