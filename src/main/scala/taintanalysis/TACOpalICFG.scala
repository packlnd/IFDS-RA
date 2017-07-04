package taintanalysis

import IFDS.HerosICFG

import org.opalj.ai.analyses.cg.CallGraph
import org.opalj.br.Method
import org.opalj.tac.Stmt

case class MStmt(s: Stmt, pc: Int, m: Method)
class TACOpalICFG(cg: CallGraph) extends HerosICFG[MStmt, Method] {
  def isCallStatement(n: MStmt): Boolean = ???

  def isExitStatement(n: MStmt): Boolean = ???

  def getReturnSitesOfCallAt(n: MStmt): Set[MStmt] = ???

  def getSuccessorsOf(n: MStmt): Set[MStmt] = ???

  def getCalleesOfCallAt(n: MStmt): Set[Method] = ???

  def getStartPointsOf(m: Method): Set[MStmt] = ???

  def getMethodOf(n: MStmt): Method = ???

  def getCallersOf(m: Method): Set[MStmt] = ???
}
