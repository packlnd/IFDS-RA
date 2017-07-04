package propagator

import IFDS.{Propagator, KillGenInfo}
import taintanalysis.{Fact, MStmt, Taint}

import org.opalj.br.{Method, ElementValue}

import org.opalj.tac._

class PrimitiveTaintKiller extends Propagator {
  def canHandle(f: Fact): Boolean = f.isInstanceOf[Taint]

  def isPrimitive(v: Expr): Boolean = {
   ??? 
  }

  def propagateCallFlow(f: Fact, callStmt: MStmt, destinationMethod: Method): KillGenInfo = {
    if (isPrimitive(f.asInstanceOf[Taint].value))
      KillGenInfo.kill
    KillGenInfo.identity
  }

  def propagateCallToReturnFlow(f: Fact, callSite: MStmt): KillGenInfo = {
    if (isPrimitive(f.asInstanceOf[Taint].value))
      KillGenInfo.kill
    KillGenInfo.identity
  }

  def propagateNormalFlow(f: Fact, curr: MStmt, succ: MStmt): KillGenInfo = {
    if (isPrimitive(f.asInstanceOf[Taint].value))
      KillGenInfo.kill
    KillGenInfo.identity
  }

  def propagateReturnFlow(
    f: Fact,
    callSite: Option[MStmt],
    calleeMethod: Method,
    exitStmt: MStmt,
    returnSite: Option[MStmt]
  ): KillGenInfo = {
    if (isPrimitive(f.asInstanceOf[Taint].value))
      KillGenInfo.kill
    KillGenInfo.identity
  }

}
