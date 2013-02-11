import optigraph.compiler._
import optigraph.library._
import optigraph.shared._

object HelloGraphCompiler extends OptiGraphApplicationCompiler with HelloGraph 
object HelloGraphInterpreter extends OptiGraphApplicationInterpreter with HelloGraph 

trait HelloGraph extends OptiGraphApplication { 
  def main() = {
    println("hello world")    
    
    val g = Graph(5, 10)
    g.addNodes()
    g.addEdge(0, 1)
    g.addEdge(0, 2)
    g.addEdge(0, 3)
    g.addEdge(1, 2)
    g.addEdge(1, 1)
    g.addEdge(3, 1)
    g.addEdge(2, 1)
    println(g.numEdges())
  }
}
