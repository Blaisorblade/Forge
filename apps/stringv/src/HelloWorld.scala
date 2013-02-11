import stringv.compiler._
import stringv.library._
import stringv.shared._

object HelloStringCompiler extends StringVApplicationCompiler with HelloString 
object HelloStringInterpreter extends StringVApplicationInterpreter with HelloString 

trait HelloString extends StringVApplication { 
  def main() = {
    println("hello world")    

    val v1 = StringV(10)
    val v2 = StringV(10)
 
    v1(0) = Array("Ja")
   
    // defs
/*
    var i = 0    
    while (i < v1.length) {
      v1(i) = "3"
      v2(i) = "4"
      println(v1(i))
      println(v2(i))
      i += 1
    }
    
    // zip
    val v3 = v1+v2
    i = 0
    println("v3 = v1+v2")
    while (i < v3.length) {
      println(v3(i))
      i += 1
    }
  
  
    val asv = new Array[StringV](2)
    asv(1) = v1
    asv(2) = v2
  */
  }
}
