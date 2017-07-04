package propagator.forwards

import IFDS.{Propagator, KillGenInfo, HerosICFG}
import taintanalysis.{Fact, Taint, Zero, MStmt, OpalICFG}

import org.opalj.br.Method

import util.Util

class ZeroAtParameterPropagator extends Propagator {
  def canHandle(f: Fact): Boolean = {
    f == Zero.ZERO
  }

  def propagateNormalFlow(f: Fact, curr: MStmt, succ: MStmt): KillGenInfo = {
    ???
  }

  def propagateCallFlow(f: Fact, callStmt: MStmt, destinationMethod: Method): KillGenInfo = {
    KillGenInfo.kill
  }

  def propagateCallToReturnFlow(f: Fact, callSite: MStmt): KillGenInfo = {
    throw new IllegalStateException()
  }

  def propagateReturnFlow(
    f: Fact,
    callSite: Option[MStmt],
    calleeMethod: Method,
    exitStmt: MStmt,
    returnSite: Option[MStmt]
  ): KillGenInfo = {
    throw new IllegalArgumentException()
  }
}
