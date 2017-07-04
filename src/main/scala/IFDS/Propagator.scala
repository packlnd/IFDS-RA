package IFDS

import taintanalysis.{MStmt, Fact}

import org.opalj.br.Method

trait Propagator {
  def canHandle(f: Fact): Boolean
  def propagateNormalFlow(f: Fact, curr: MStmt, succ: MStmt): KillGenInfo
  def propagateCallFlow(f: Fact, callStmt: MStmt, destinationMethod: Method): KillGenInfo
  def propagateCallToReturnFlow(f: Fact, callSite: MStmt): KillGenInfo
  def propagateReturnFlow(
    f: Fact,
    callSite: Option[MStmt],
    calleeMethod: Method,
    exitStmt: MStmt,
    returnSite: Option[MStmt]
  ): KillGenInfo
}

case class KillGenInfo(val killed: Boolean, val gen: Set[_ <: Fact])

object KillGenInfo {
  val id = new KillGenInfo(false, Set())
  def identity: KillGenInfo = id
  def kill: KillGenInfo = new KillGenInfo(true, Set())
  def gen(facts: Fact*): KillGenInfo = {
    if (facts.length == 0) {throw new IllegalArgumentException()}
    new KillGenInfo(false, facts.toSet)
  }
  def propagate(facts: Fact*): KillGenInfo = {
    if (facts.length == 0) {throw new IllegalArgumentException()}
    new KillGenInfo(true, facts.toSet)
  }
}
