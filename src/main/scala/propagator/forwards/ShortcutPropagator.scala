package propagator.forwards

import IFDS.{KillGenInfo}
import taintanalysis.{Fact, MStmt, Taint}
import org.opalj.tac._

import propagator.MethodFilteringPropagator

import org.opalj.br.Method

import util.Util

class ShortcutPropagator extends MethodFilteringPropagator(???) {

  override def handleMethodCall(t: Taint, callSite: MStmt, call: Call): KillGenInfo = {
    val signature = Util.getSignature(call)
    if (callSite.s.isInstanceOf[Assignment]) {
      val assignment = callSite.s.asInstanceOf[Assignment]
      val expr = Util.getInvokeExpr(callSite).s.asInstanceOf[Call].params(0)
      if (Util.maybeSameLocation(t.value, expr)) {
        val retValue = assignment.targetVar
        return KillGenInfo.propagate(new Taint(callSite, t, retValue, retValue.cTpe))
      }
    }
    return KillGenInfo.identity
  }
}
