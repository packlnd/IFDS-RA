package propagator.backwards

import IFDS.{Propagator, KillGenInfo}
import taintanalysis.{Fact, MStmt}

import org.opalj.br.Method

class StringBuilderPropagator extends Propagator {
  def canHandle(f: Fact): Boolean = ???
  def propagateCallFlow(f: Fact, callStmt: MStmt, destinationMethod: Method): KillGenInfo = ???
  def propagateCallToReturnFlow(f: Fact, callSite: MStmt): KillGenInfo = ???
  def propagateNormalFlow(f: Fact, curr: MStmt, succ: MStmt): KillGenInfo = ???
  def propagateReturnFlow(
    f: Fact,
    callSite: Option[MStmt],
    calleeMethod: Method,
    exitStmt: MStmt,
    returnSite: Option[MStmt]
  ): KillGenInfo = ???
}
