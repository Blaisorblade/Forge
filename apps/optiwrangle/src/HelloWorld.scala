import optiwrangle.compiler._
import optiwrangle.library._
import optiwrangle.shared._

object HelloWranglerCompiler extends OptiWrangleApplicationCompiler with HelloWrangler 
object HelloWranglerInterpreter extends OptiWrangleApplicationInterpreter with HelloWrangler 

trait HelloWrangler extends OptiWrangleApplication { 
  def main() = {
    println("hello world")    
    val dw = DataWrangler("/afs/cs.stanford.edu/u/gibbons4/data/Flickr_10--130_to_25--120_pulled_2012-10-01.csv", "\n", ",")
    dw.cut("\"")
    dw.write_to_file("/afs/cs.stanford.edu/u/gibbons4/data/test.out") 
    
    // if side effect updates are too unwieldy it will instead look like
    //val dw0 = DataWrangler("/afs/cs.stanford.edu/u/gibbons4/data/Flickr_10--130_to_25--120_pulled_2012-10-01.csv", "\n", ",")
    //val dw1 = dw0.cut("\"")
    //dw1.write_to_file("/afs/cs.stanford.edu/u/gibbons4/data/test.out") 
  }
}
