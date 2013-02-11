package ppl.dsl.forge
package optigraph

import core.{ForgeApplication,ForgeApplicationRunner}
//import templates.Utilities.nl

object OptiGraphDSL extends ForgeApplicationRunner with OptiGraph

trait OptiGraph extends ForgeApplication with ScalaOps with GraphOps with NodeOps {

  def dslName = "OptiGraph"

  def specification() = {
    addScalaOps()
    addGraphOps()
    addNodeOps()
  }
}

