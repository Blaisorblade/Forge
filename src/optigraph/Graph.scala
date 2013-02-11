package ppl.dsl.forge
package optigraph

//  why?
import core.{ForgeApplication,ForgeApplicationRunner}

trait GraphOps extends ForgeApplication {

  def addGraphOps() {
    // Declare Graph type
    val Graph = tpe("Graph")

    // Graph datastructure
    data(Graph, List(), ("_numEdges", MInt))

    // Ops
    val graph_new = op (Graph) ("apply", static, List(), List(MInt), Graph, codegenerated)
    //val graph_toString = op (Graph) ("toString", static, List(), List(), MString, codegenerated)
    val graph_numEdges = op (Graph) ("numEdges", infix, List(), List(Graph), MInt, codegenerated)

    // CodeGen
    val stream = ForgePrinter()
    codegen (graph_new) ($cala, "new "+graph_new.tpeName+"("+quotedArg(0)+")")
    //codegen (graph_toString) ($cala, stream.printLines("\"\n  Number of edges :"+quotedArg(0)+"\n\""))
    codegen (graph_numEdges) ($cala, stream.printLines(quotedArg(0)+"._numEdges"))
  }

}

