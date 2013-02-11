package ppl.dsl.forge
package optigraph

//  why?
import core.{ForgeApplication,ForgeApplicationRunner}

trait NodeOps extends ForgeApplication with GraphOps {

  def addNodeOps() {
    addGraphOps()
    
    // Declare Node type
    val Node = tpe("Node")

    // Node datastructure
    //data(Node, List(), ("_g", Graph), ("_id", MInt))
    data(Node, List(), ("_id", MInt))

    // Ops
    val node_new = op (Node) ("apply", static, List(), List(MInt), Node, codegenerated)

    // CodeGen
    val stream = ForgePrinter()
    codegen (node_new) ($cala, "new "+node_new.tpeName+"("+quotedArg(0)+","+quotedArg(1)+")")
  }

}

