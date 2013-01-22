package ppl.dsl.forge
package examples

import core.{ForgeApplication, ForgeApplicationRunner}

object OptiWrangleRunner extends ForgeApplicationRunner with OptiWrangle

trait OptiWrangle extends ForgeApplication with ScalaOps {

  //name of the DSL
  def dslName = "OptiWrangle"

  //DSL specification
  def specification() = {
    // Include Scala Ops
    addScalaOps()

    //Types
    val T = tpePar("T")
    val DataWrangler = tpe("DataWrangler")
    //val AS = tpeInst(GArray(T), List(MString))
    val AAS = tpe("AAS", List(T))

    //Data Structures
    data(DataWrangler, List(), ("_inFile", MString), ("_rows", MString), ("_cols", MString), 
      ("_table", AAS))
    data(AAS, List(T), ("_data", GArray(T)), ("_length", MInt))
    //data(AS, List(), ("_fake_nothing", MInt))
    //val as_fake = op (AS) ("fake", static, List(), List(AS), MUnit, codegenerated)
    //codegen (as_fake) ($cala, "()")

    /**
     * DeliteCollectionification
     * This enables a tpe to be passed in as the collection type of a Delite op
     */ 

    //Ops
    val stream = ForgePrinter()

    // -- DataWrangler
    val dw_new = op (DataWrangler) ("apply", static, List(), List(MString, MString, MString), DataWrangler, codegenerated)
    //val dw_new_2 = op (DataWrangler) ("apply", static, List(), List(AAS), DataWrangler, codegenerated)
    //val dw_cut = op (DataWrangler) ("cut", infix, List(), List(DataWrangler, MString), DataWrangler, codegenerated)
    val dw_cut = op (DataWrangler) ("cut", infix, List(), List(DataWrangler, MString), MUnit, single(MUnit, {
  stream.printLines(
  "_table = _table.cut("+quotedArg(1)+")",
  "()"
  )}))
    //val dw_drop = op (DataWrangler) ("drop", infix, List(), List(DataWrangler, MInt), DataWrangler, codegenerated)
    //val dw_drop_map = op (DataWrangler) ("drop_map", infix, List(), List(AAS, MInt), AAS, map((AS, AS, AAS), 0, "e => drop_impl(e,"+quotedArg(1)+")")) 
    //val dw_drop_impl = op (DataWrangler) ("drop_impl", infix, List(), List(DataWrangler, AS, MInt), AS, codegenerated) 
    //val dw_delete = op (DataWrangler) ("delete", infix, List(), List(DataWrangler, MInt, MString, MInt), DataWrangler, codegenerated)
    val dw_write_to_file = op (DataWrangler) ("write_to_file", infix, List(), List(DataWrangler, MString), MUnit, codegenerated)
  
    //Code Generators - these operations are likely not supported by Forge

    // -- Datawrangler 
    // from file
    codegen (dw_new) ($cala, stream.printLines("val table = scala.io.Source.fromFile("+quotedArg(0)+").mkString.split("+quotedArg(1)+").map(x=> x.split("+quotedArg(2)+"))",
    "val aas = new AAS[String](table.length, table)",
    "new "+dw_new.tpeName+"("+quotedArg(0)+", "+quotedArg(1)+", "+quotedArg(2)+", aas)"))
    // from AAS
    //codegen (dw_new_2) ($cala, "new "+dw_new_2.tpeName+"(\"\",\"\",\"\","+quotedArg(0)+")")

    //todo - strings vs. regex - see what dw does
    //codegen (dw_cut) ($cala, "new "+dw_cut.tpeName+"(\"\",\"\",\"\","+quotedArg(0)+"._table.cut("+quotedArg(1)+"))")
    //codegen (dw_drop) ($cala, "new "+dw_drop.tpeName+"(\"\",\"\",\"\","+quotedArg(0)+".table.drop_map("+quotedArg(1)+"))")
    //codegen (dw_drop_impl) ($cala, stream.printLines("val y = "+quotedArg(1)+".toBuffer", "y.remove("+quotedArg(2)+")", "y.toArray"))
    //codegen (dw_delete) ($cala, "new "+dw_delete.tpeName+"(\"\",\"\",\"\","+quotedArg(0)+"._table filter (x => !(x("+quotedArg(1)+").indexOf("+quotedArg(2)+") == "+quotedArg(3)+")))")

    codegen (dw_write_to_file) ($cala, stream.printLines("val outFile = new java.io.PrintStream(new java.io.FileOutputStream("+quotedArg(1)+"))",
    quotedArg(0)+"._table._data foreach { row =>",
    "  outFile.println(row.mkString(\"\",\"\"))",
    "}",
    "outFile.close()"))

    // -- AAS - same as vector
    val aas_new = op (AAS) ("apply", static, List(T), List(MInt), AAS, codegenerated, effect = mutable)
    val aas_length = op (AAS) ("length", infix, List(T), List(AAS), MInt, codegenerated)
    val aas_apply = op (AAS) ("apply", infix, List(T), List(AAS,MInt), T, codegenerated)
    val aas_update = op (AAS) ("update", infix, List(T), List(AAS,MInt,T), MUnit, codegenerated, effect = write(0))

    val aas_cut = op (AAS) ("cut", infix, List(T), List(AAS, MString), AAS, map((MAS, MAS, AAS), 0, "e => e map (x => x.replaceAll("+quotedArg(1)+", \"\"))"))

    codegen (aas_new) ($cala, "new "+aas_new.tpeName+"[Array[String]]("+quotedArg(0)+", new Array[Array[String]]("+quotedArg(0)+"))")
    codegen (aas_length) ($cala, quotedArg(0) + "._length")
    codegen (aas_apply) ($cala, quotedArg(0) + "._data.apply(" + quotedArg(1) + ")")
    codegen (aas_update) ($cala, quotedArg(0) + "._data.update(" + quotedArg(1) + ", " + quotedArg(2) + ")")

    AAS is DeliteCollection(T, aas_new, aas_length, aas_apply, aas_update)

    ()
  }
}

