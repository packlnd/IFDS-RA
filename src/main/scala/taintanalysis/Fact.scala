package taintanalysis

import org.opalj.tac._
import org.opalj.br.ComputationalType

import scala.collection.mutable.{Set => MSet}

abstract class Fact {
  val payloads: MSet[Any] = MSet()
  def createAlias(n: MStmt): Fact
  def addPayload(s: String): Unit = {
    payloads.add(s)
  }
}

class Taint(val stmt: MStmt, val t: Taint, val value: Expr, val tpe: ComputationalType) extends Fact {
  def createAlias(n: MStmt): Fact = ???
}

class Zero extends Fact {
  override def createAlias(f: MStmt): Fact = {
    ???
  }
  override def toString: String = "Zero"
}

object Zero {
  val ZERO: Fact = new Zero()
}
