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
    val Table = tpe("Table")
    val AS = tpeInst(GArray(tpePar("T")), List(MString))
    val AAS = tpeInst(GArray(tpePar("T")), List(AS))
  
    data(Table, List(), ("_length", MInt), ("_data", AAS))
    
    val stream = ForgePrinter()

    val as_new = op (AS) ("apply", static, List(), List(MInt), AS, codegenerated, effect = mutable)
    codegen (as_new) ($cala, "new"+as_new.tpeName+"("+quotedArg(0)+")")

    val table_new = op (Table) ("apply", static, List(), List(MInt, AAS), Table, codegenerated, effect = mutable)
    val table_new_empty = op (Table) ("apply", static, List(), List(MInt), Table, codegenerated, effect = mutable)
    val table_length = op (Table) ("length", infix, List(), List(Table), MInt, codegenerated) 
    val table_apply = op (Table) ("apply", infix, List(), List(Table, MInt), AS, codegenerated)
    val table_update = op (Table) ("update", infix, List(), List(Table, MInt, AS), MUnit, codegenerated, effect= write(0))

    codegen (table_new) ($cala, "new "+table_new.tpeName+"("+quotedArg(0)+","+quotedArg(1)+")")
    codegen (table_new_empty) ($cala, "new "+table_new.tpeName+"("+quotedArg(0)+", new Array[Array[String]]("+quotedArg(0)+"))")
    codegen (table_length) ($cala, quotedArg(0) + "._length")
    codegen (table_apply) ($cala, quotedArg(0) + "._data.apply("+quotedArg(1)+")")
    codegen (table_update) ($cala, quotedArg(0) + "._data.update("+quotedArg(1)+", "+quotedArg(2)+")")

    Table is DeliteCollection(AS, table_new_empty, table_length, table_apply, table_update)

    // Start DW
    /*
    val DataWrangler = tpe("DataWrangler")

    val dw_new_csv = op (DataWrangler) ("apply", static, List(), List(MString, MString, MString), DataWrangler, single(DataWrangler, { stream.printLines(
    "val table = scala.io.Source.fromFile("+quotedArg(0)+").mkString.split("+quotedArg(1)+").map(x=> x.split("+quotedArg(2)+"))",
    "val aas = AAS(table.length, table)",
    "new "+tpeName+"("+quotedArg(0)+", "+quotedArg(1)+", "+quotedArg(2)+", aas)")}),
    effect = mutable)
    */

    ()
  }
}
