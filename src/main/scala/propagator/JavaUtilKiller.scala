package propagator

import IFDS.{Propagator, KillGenInfo, HerosICFG}
import taintanalysis.{Fact, Taint, MStmt, TACOpalICFG, Context}

import util.Util

import org.opalj.br.Method
import org.opalj.tac.MethodCall

class JavaUtilKiller(context: Context) extends Propagator {
  //
  // DONE
  //
  def canHandle(f: Fact): Boolean = {
    f.isInstanceOf[Taint]
  }

  def propagateNormalFlow(f: Fact, curr: MStmt, succ: MStmt): KillGenInfo = {
    KillGenInfo.identity
  }

  def propagateCallFlow(f: Fact, callStmt: MStmt, destinationMethod: Method): KillGenInfo = {
    val ie = Util.getInvokeExpr(callStmt)
    if (isJavaUtilMethod(ie.m)) {
      KillGenInfo.kill
    } else {
      KillGenInfo.identity
    }
  }

  def propagateCallToReturnFlow(f: Fact, callSite: MStmt): KillGenInfo = {
    KillGenInfo.identity
  }

  def propagateReturnFlow(
    f: Fact,
    callSite: Option[MStmt],
    calleeMethod: Method,
    exitStmt: MStmt,
    returnSite: Option[MStmt]
  ): KillGenInfo = {
    if (returnSite.isDefined && isJavaUtilMethod(context.icfg.getMethodOf(returnSite.get))) {
      KillGenInfo.kill
    } else {
      KillGenInfo.identity
    }
  }

  def isJavaUtilMethod(m: Method): Boolean = {
    for (im <- Util.getInitialDeclaration(m)) {
      val pkg = context.p.classFile(im).thisType.packageName
      if (pkg.equals("java/util") || pkg.startsWith("java/util/concurrent")) {
        return true
      }
    }
    return false
  }
}
