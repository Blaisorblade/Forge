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
    val AS = tpeInst(GArray(T), List(MString))
    val ASS = tpeInst(GArray(T), List(AS))

    //Data Structures
    data(DataWrangler, List(), ("_inFile", MString), ("_rows", MString), ("_cols", MString), 
      ("_table", ASS))

    //Ops
    val stream = ForgePrinter()

    // -- DataWrangler
    val dw_new = op (DataWrangler) ("apply", static, List(), List(MString, MString, MString), DataWrangler, codegenerated)
    val dw_new_2 = op (DataWrangler) ("apply", static, List(), List(ASS), DataWrangler, codegenerated)
    val dw_cut = op (DataWrangler) ("cut", infix, List(), List(DataWrangler, MString), DataWrangler, codegenerated)
    val dw_drop = op (DataWrangler) ("drop", infix, List(), List(DataWrangler, MInt), DataWrangler, codegenerated)
    val dw_delete = op (DataWrangler) ("delete", infix, List(), List(DataWrangler, MInt, MString, MInt), DataWrangler, codegenerated)
    val dw_write_to_file = op (DataWrangler) ("write_to_file", infix, List(), List(DataWrangler, MString), MUnit, codegenerated)
  
    //Code Generators - these operations are likely not supported by Forge
    // -- Datawrangler 
    codegen (dw_new) ($cala, stream.printLines("val table = scala.io.Source.fromFile("+dw_new.quotedArg(0)+").mkString.split("+dw_new.quotedArg(1)+").map(x=> x.split("+dw_new.quotedArg(2)+"))",
    "new "+dw_new.tpeName+"("+dw_new.quotedArg(0)+", "+dw_new.quotedArg(1)+", "+dw_new.quotedArg(2)+", table)"), isSimple=false)
    codegen (dw_new_2) ($cala, "new "+dw_new_2.tpeName+"(\"\",\"\",\"\","+dw_new.quotedArg(0)+")")

    //todo - strings vs. regex - see what dw does
    codegen (dw_cut) ($cala, "new "+dw_cut.tpeName+"(\"\",\"\",\"\","+dw_cut.quotedArg(0)+"._table map (y => y map (x => x.replaceAll("+dw_cut.quotedArg(1)+", \"\"))))")
    codegen (dw_drop) ($cala, "new "+dw_cut.tpeName+"(\"\",\"\",\"\","+dw_drop.quotedArg(0)+"._table map (y => {val z = y.toBuffer ; z.remove("+dw_drop.quotedArg(1)+") ; z.toArray}))")
    codegen (dw_delete) ($cala, "new "+dw_cut.tpeName+"(\"\",\"\",\"\","+dw_delete.quotedArg(0)+"._table filter (x => !(x("+dw_delete.quotedArg(1)+").indexOf("+dw_delete.quotedArg(2)+") == "+dw_delete.quotedArg(3)+")))")

    codegen (dw_write_to_file) ($cala, stream.printLines("val outFile = new java.io.PrintStream(new java.io.FileOutputStream("+dw_write_to_file.quotedArg(1)+"))",
    dw_write_to_file.quotedArg(0)+"._table foreach { row =>",
    "  outFile.println(row.mkString(\"\\\",\\\"\"))",
    "}",
    "outFile.close()"), isSimple=false)

    ()
  }
}


