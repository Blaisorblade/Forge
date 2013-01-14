import optiwrangle.compiler._
import optiwrangle.library._
import optiwrangle.shared._

object HelloWranglerCompiler extends OptiWrangleApplicationCompiler with HelloWrangler 
object HelloWranglerInterpreter extends OptiWrangleApplicationInterpreter with HelloWrangler 

trait HelloWrangler extends OptiWrangleApplication { 
  def main() = {
    println("hello world")    
   
    val table0 = DataWrangler("/afs/cs.stanford.edu/u/gibbons4/data/Flickr_10--130_to_25--120_pulled_2012-10-01.csv", "\n", ",")
    val table1 = table0.cut("\"")
    val table2 = table1.drop(0)
    val table3 = table2.delete(1, "-126.", 0)
    table3.write_to_file("/afs/cs.stanford.edu/u/gibbons4/data/test.out") 
  }
}
