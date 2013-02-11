import intvector.compiler._
import intvector.library._
import intvector.shared._

object HelloStringCompiler extends StringVectorApplicationCompiler with HelloString 
object HelloStringInterpreter extends StringVectorApplicationInterpreter with HelloString 

trait HelloString extends StringVectorApplication { 
  def main() = {
    println("hello world")    

    val v1 = Vector(10)
    val v2 = Vector(10)
    
    // defs
    var i = 0    
    while (i < v1.length) {
      v1(i) = 3
      v2(i) = 4
      //println("v1(" + i + "): " + v1(i))
      //println("v1(" + i + "): " + v2(i))
      println(v1(i))
      println(v2(i))
      i += 1
    }
    
    // zip
    val v3 = v1+v2
    i = 0
    println("v3 = v1+v2")
    while (i < v3.length) {
      //println("v3(" + i + "): " + v3(i))
      println(v3(i))
      i += 1
    }
  }
}
