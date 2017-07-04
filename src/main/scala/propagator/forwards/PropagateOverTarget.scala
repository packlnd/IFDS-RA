package propagator.forwards

import IFDS.{Propagator, KillGenInfo, HerosICFG}
import taintanalysis._

import org.opalj.br.Method

import scala.collection.mutable.{Set => MSet}

import org.opalj.tac._



import util.Util

class PropagateOverTarget(context: Context) extends Propagator {
  def canHandle(f: Fact): Boolean = {
    f.isInstanceOf[Taint]
  }

  def propagateNormalFlow(f: Fact, curr: MStmt, succ: MStmt): KillGenInfo = {
    KillGenInfo.identity
  }

  def propagateCallFlow(f: Fact, callStmt: MStmt, destinationMethod: Method): KillGenInfo = {
    if (Util.isTarget(context, callStmt)) {
      KillGenInfo.kill
    } else {
      KillGenInfo.identity
    }
  }

  def propagateCallToReturnFlow(f: Fact, callSite: MStmt): KillGenInfo = {
    if (Util.isTarget(context, callSite)) {
      val taint = f.asInstanceOf[Taint]
      val call = Util.getInvokeExpr(callSite)
      var argsTainted = false
      var receiverTainted = false
      if (call.s.isInstanceOf[InstanceMethodCall]) {
        val receiver = call.s.asInstanceOf[InstanceMethodCall].receiver
        if (receiver.equals(taint.value)) {
          receiverTainted = true
        }
      }

      for (arg <- call.s.asInstanceOf[Call].params) {
        argsTainted = argsTainted || arg.equals(taint.value)
      }
      if (argsTainted || receiverTainted) {
        val newTaints: MSet[Taint] = MSet()
        if (callSite.s.isInstanceOf[Assignment]) {
          val left = callSite.s.asInstanceOf[Assignment].targetVar
          val retValueTaint = new Taint(callSite, taint, left, left.cTpe)
          if (context.tpe != InnerToOuter) {
            retValueTaint.addPayload("passedSink")
          }
          newTaints.add(retValueTaint)
        }
        if (!receiverTainted && call.isInstanceOf[InstanceMethodCall]) {
          val receiver = call.asInstanceOf[InstanceMethodCall].receiver
          newTaints.add(new Taint(callSite, taint, receiver, receiver.cTpe))
        }
        return KillGenInfo(false, newTaints.toSet)
      }
    }
    return KillGenInfo.identity
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
}
