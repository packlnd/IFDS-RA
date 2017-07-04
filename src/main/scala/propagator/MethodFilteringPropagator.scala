package propagator

import taintanalysis._
import IFDS.{KillGenInfo, Propagator}

import util.Util

import org.opalj.tac._
import org.opalj.br.Method

abstract class MethodFilteringPropagator(methodSigs: String*) extends Propagator {
  def canHandle(f: Fact): Boolean = {
    f.isInstanceOf[Taint]
  }

  def propagateNormalFlow(f: Fact, curr: MStmt, succ: MStmt): KillGenInfo = {
    KillGenInfo.identity
  }

  def propagateCallFlow(f: Fact, callStmt: MStmt, destinationMethod: Method): KillGenInfo = {
    if (callStmt.s.isInstanceOf[Assignment]) {
      val call = Util.getInvokeExpr(callStmt).s.asInstanceOf[Call]
      val signature = Util.getSignature(call)
      if (methodSigs.contains(signature)) {
        return KillGenInfo.kill
      }
    }
    return KillGenInfo.identity
  }

  def propagateCallToReturnFlow(f: Fact, callSite: MStmt): KillGenInfo = {
    val call = Util.getInvokeExpr(callSite).s.asInstanceOf[Call]
    val signature = Util.getSignature(call)
    if (methodSigs.contains(signature)) {
      handleMethodCall(f.asInstanceOf[Taint], callSite, call)
    } else {
      KillGenInfo.identity
    }
  }

  def propagateReturnFlow(
    f: Fact,
    callSite: Option[MStmt],
    calleeMethod: Method,
    exitStmt: MStmt,
    returnSite: Option[MStmt]
  ): KillGenInfo = {
    KillGenInfo.identity
  }

  def handleMethodCall(t: Taint, callSite: MStmt, invokeExpr: Call): KillGenInfo
}
