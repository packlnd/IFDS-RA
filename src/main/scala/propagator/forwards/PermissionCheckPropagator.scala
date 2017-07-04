package propagator.forwards

import IFDS.{Propagator, KillGenInfo, HerosICFG}
import taintanalysis.MStmt
import taintanalysis.Fact
import taintanalysis.Taint

import org.opalj.br.Method

import org.opalj.tac._

import util.Util

class PermissionCheckPropagator extends Propagator {
  def canHandle(f: Fact): Boolean = {
    f.isInstanceOf[Taint]
  }

  def propagateNormalFlow(f: Fact, curr: MStmt, succ: MStmt): KillGenInfo = {
    KillGenInfo.identity
  }

  def propagateCallFlow(f: Fact, callStmt: MStmt, destinationMethod: Method): KillGenInfo = {
    KillGenInfo.identity
  }

  def propagateCallToReturnFlow(f: Fact, callSite: MStmt): KillGenInfo = {
    val taint = f.asInstanceOf[Taint]
    val call = Util.getInvokeExpr(callSite).s.asInstanceOf[Call]
    if (
      call.declaringClass.equals("sun/reflect/misc/reflectUtil") ||
      call.declaringClass.toJava.equals("java/lang/SecurityManager") &&
      call.name.equals("checkPackageAccess") &&
      call.params(0).equals(taint.value)
    ) {
      KillGenInfo.kill
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
}
