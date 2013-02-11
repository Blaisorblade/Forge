package ppl.dsl.forge
package examples

import core.{ForgeApplication,ForgeApplicationRunner}

object OptiGraphDSL extends ForgeApplicationRunner with OptiGraph

trait OptiGraph extends ForgeApplication with ScalaOps {

  def dslName = "OptiGraph"

  def specification() = {
    addScalaOps()

    // Types
    val Graph = tpe("Graph")
    val Node = tpe("Node")
    val Edge = tpe("Edge")
    val Nodes = tpeInst(GArray(tpePar("T")), List(Node))  
    val Edges = tpeInst(GArray(tpePar("T")), List(Edge))  

    val T = tpePar("T")

    val NodeProperty = tpe("NodeProperty", List(T))  
    val NP = NodeProperty
    val EdgeProperty = tpe("EdgeProperty", List(T))
    //val GSet = tpe("GSet", List(T))
    //val GOrder = tpe("GOrder", List(T))
    //val GSeq = tpe("GSeq", List(T))
    val GIterable = tpe("GIterable", List(T))
    //val Reduceable = tpe("Reducable", List(T))
    //val Deferrable = tpe("Deferrable", List(T))

    // Data Structures
    data(Graph, List(), ("_numNodes", MInt), ("_numEdges", MInt), ("_nodes", Nodes), ("_edges", Edges), ("_setEdges", MInt))
    data(Node, List(), ("_g", Graph), ("_id", MInt))
    data(Edge, List(), ("_g", Graph), ("_u", Node), ("_v", Node)) //from/to vs. u/v

    // Constructors
    val graph_new = op (Graph) ("apply", static, List(), List(MInt, MInt), Graph, codegenerated, effect=mutable)
    //val graph_new_0 = op (Graph) ("apply", static, List(), List(), Graph, codegenerated)
    val node_new = op (Node) ("apply", static, List(), List(Graph, MInt), Node, codegenerated)
    val edge_new_int = op (Edge) ("apply", static, List(), List(Graph, MInt, MInt), Edge, codegenerated)
    val edge_new_node = op (Edge) ("apply", static, List(), List(Graph, Node, Node), Edge, codegenerated)

    // Prints code
    val stream = ForgePrinter()

    // Codegenerators
    codegen (graph_new) ($cala, stream.printLines("val nodes = new Array[Node]("+quotedArg(0)+")",
      //"for(i <- 0 until "+quotedArg(0)+") nodes(i) = new Node(this, i)",
      "val edges = new Array[Edge]("+quotedArg(1)+")",
      "new "+graph_new.tpeName+"("+quotedArg(0)+","+quotedArg(1)+", nodes, edges, 0)"))
    codegen (node_new) ($cala, "new "+node_new.tpeName+"("+quotedArg(0)+","+quotedArg(1)+")")
    codegen (edge_new_node) ($cala, "new "+edge_new_node.tpeName+"("+quotedArg(0)+","+quotedArg(1)+","+quotedArg(2)+")")
    codegen (edge_new_int) ($cala, stream.printLines(
      "val u = "+quotedArg(0)+".node("+quotedArg(1)+")",
      "val v = "+quotedArg(0)+".node("+quotedArg(2)+")",
      "new "+edge_new_int.tpeName+"("+quotedArg(0)+",u,v)"))

    // Operations on graphs
    val graph_numNodes = op (Graph) ("numNodes", infix, List(), List(Graph), MInt, codegenerated)
    codegen (graph_numNodes) ($cala, quotedArg(0)+"._numNodes")
    val graph_numEdges = op (Graph) ("numEdges", infix, List(), List(Graph), MInt, codegenerated)
    codegen (graph_numEdges) ($cala, quotedArg(0)+"._numEdges")
    
    val graph_addNodes = op (Graph) ("addNodes", infix, List(), List(Graph), MUnit, codegenerated)
    codegen (graph_addNodes) ($cala, "for (i <- 0 until "+quotedArg(0)+".numNodes()) "+quotedArg(0)+"._nodes(i) = new Node("+quotedArg(0)+",i)")

    val graph_addEdge = op (Graph) ("addEdge", infix, List(), List(Graph, MInt, MInt), MUnit, codegenerated)   
    codegen (graph_addEdge) ($cala, stream.printLines(quotedArg(0)+"._edges.update("+quotedArg(0)+"._setEdges, new Edge("+quotedArg(0)+","+quotedArg(0)+".node("+quotedArg(1)+"),"+quotedArg(0)+".node("+quotedArg(2)+")))", quotedArg(0)+"._setEdges += 1"))
    /*
    codegen (graph_addEdge) ($cala, stream.printLines(
      quotedArg(0)+"._edges.update("+quotedArg(0)+"._setEdges, new Edge("+quotedArg(0)+","+quotedArg(1)+","+quotedArg(2)+"))", 
      quotedArg(0)+"._setEdges += 1"))
    */

    val graph_node = op (Graph) ("node", infix, List(), List(Graph, MInt), Node, codegenerated)
    codegen (graph_node) ($cala, quotedArg(0) + "._nodes("+quotedArg(1)+")")

    val graph_nodes = op (Graph) ("nodes", infix, List(), List(Graph), Nodes, codegenerated)
    
  
    // Operations on nodes
    val node_id = op (Node) ("id", infix, List(), List(Node), MInt, codegenerated)
    codegen (node_id) ($cala, quotedArg(0)+"._id")

    ()
  }
}

