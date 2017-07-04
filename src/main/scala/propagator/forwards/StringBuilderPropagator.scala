package propagator.forwards

import IFDS.{KillGenInfo}
import taintanalysis._
import propagator.MethodFilteringPropagator
import org.opalj.br.Method
import util.Util
import org.opalj.tac._
import scala.collection.mutable.{Set => MSet}

class StringBuilderPropagator extends MethodFilteringPropagator(
  ???,
  ???,
  ???,
  ???,
  ???,
  ???
) {
  override def handleMethodCall(t: Taint, callSite: MStmt, call: Call): KillGenInfo = {
    if (isStringBuilderToString(call)) {
      if (callSite.s.isInstanceOf[Assignment]) {
        val assignment = callSite.asInstanceOf[Assignment]
        val receiver = call.asInstanceOf[InstanceMethodCall].receiver
        if (Util.maybeSameLocation(t.value, receiver)) {
          return KillGenInfo.gen(new Taint(callSite, t, assignment.targetVar, assignment.targetVar.cTpe))
        }
      }
    } else if (isStringBuilderAppend(call)) {
      val receiver = call.asInstanceOf[InstanceMethodCall].receiver
      val param = call.params(0)
      var genReturn = false
      var callReceiverTainted = false
      if (Util.maybeSameLocation(t.value, receiver)) {
        genReturn = true
        callReceiverTainted = true
      }
      if (Util.maybeSameLocation(t.value, param))
        genReturn = true
      val genTaints: MSet[Taint] = MSet()
      if (genReturn && callSite.s.isInstanceOf[Assignment]) {
        val retVal = callSite.asInstanceOf[Assignment].targetVar
        if (!Util.maybeSameLocation(t.value, retVal))
          genTaints.add(new Taint(callSite, t, retVal, retVal.cTpe))
        if (receiver.equals(retVal))
          callReceiverTainted = true
      }
      if (!callReceiverTainted)
        genTaints.add(new Taint(callSite, t, receiver, receiver.cTpe))
      return new KillGenInfo(false, genTaints.toSet)
    } else if (isStringBuilderConstructorCall(call)) {
      val stringBuilder = call.asInstanceOf[InstanceMethodCall].receiver
      if (call.params.size == 1) {
        val param = call.params(0)
        if (Util.maybeSameLocation(t.value, param))
          return KillGenInfo.gen(new Taint(callSite, t, stringBuilder, stringBuilder.cTpe))
      }
    }
    return KillGenInfo.identity
  }

  def isStringBuilderToString(call: Call): Boolean = {
    ???
  }

  def isStringBuilderAppend(call: Call): Boolean = {
    ???
  }

  def isStringBuilderConstructorCall(call: Call): Boolean = {
    ???
  }
}
