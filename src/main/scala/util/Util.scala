package util


import org.opalj.tac._
import org.opalj.br.Method

import taintanalysis.{MStmt, Context}

object Util {
  def containsInvokeExpr(s: Stmt): Boolean = {
    ???
  }

  def getInvokeExpr(s: MStmt): MStmt = {
    ???
  }

  def isSink(s: Stmt): Boolean = {
    ???
  }

  def getSignature(c: Call): String = {
    ???
  }

  def getInitialDeclaration(m: Method): Set[Method]= {
    ???
  }

  def maybeSameLocation(e1: Expr, e2: Expr): Boolean = {
    ???
  }

  def isTarget(context: Context, stmt: MStmt): Boolean = {
    ???
  }

  def runGC: Unit = {
    org.opalj.util.gc()
  }
}
