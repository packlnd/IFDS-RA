package taintanalysis

import org.opalj.br.Method

trait Visitor[T] {
  def visit(normalEdge: NormalEdge): T
  def visit(callEdge: CallEdge): T
  def visit(returnEdge: ReturnEdge): T
  def visit(callToReturnEdge: CallToReturnEdge): T
}

abstract class Edge {
  def accept[T](visitor: Visitor[T]): T
}

class NormalEdge(val curr: MStmt, val succ: MStmt) extends Edge {
  override def accept[T](visitor: Visitor[T]): T = {
    return visitor.visit(this)
  }
}

class CallEdge(val callStmt: MStmt, val destinationMethod: Method) extends Edge {
  override def accept[T](visitor: Visitor[T]): T = {
    return visitor.visit(this)
  }
}

class ReturnEdge(
  val callSite: Option[MStmt],
  val calleeMethod: Method,
  val exitStmt: MStmt,
  val returnSite: Option[MStmt]
) extends Edge {
  override def accept[T](visitor: Visitor[T]): T = {
    return visitor.visit(this)
  }
}

class CallToReturnEdge(val callSite: MStmt, val returnSite: MStmt) extends Edge {
  override def accept[T](visitor: Visitor[T]): T = {
    return visitor.visit(this)
  }
}
