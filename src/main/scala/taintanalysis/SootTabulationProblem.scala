package taintanalysis

import flow.twist.config.AnalysisConfiguration
import flow.twist.ifds.{TabulationProblem => FTProblem}
import flow.twist.trackable._

import scala.collection.JavaConversions._

import IFDS.{HerosICFG, IFDSProblem, FlowFunction, FlowFunctions, Propagator}

import soot.{Unit=>SUnit, SootMethod}

class SootTabulationProblem(config: AnalysisConfiguration, threads: Int) extends IFDSProblem[SUnit, Trackable, SootMethod] {
  val ftp = new FTProblem(config, threads)
  def getContext() = ftp.getContext()
  val ftpIcfg = ftp.interproceduralCFG()
  val ftpFFs = ftp.flowFunctions()
  def getIcfg: HerosICFG[SUnit, SootMethod] = new HerosICFG[SUnit, SootMethod] {
    override def isCallStatement(n: SUnit): Boolean = ftpIcfg.isCallStmt(n)
    override def isExitStatement(n: SUnit): Boolean = ftpIcfg.isExitStmt(n)
    override def getReturnSitesOfCallAt(n: SUnit): Set[SUnit] = ftpIcfg.getReturnSitesOfCallAt(n).toSet
    override def getSuccessorsOf(n: SUnit): Set[SUnit] = ftpIcfg.getSuccsOf(n).toSet
    override def getCalleesOfCallAt(n: SUnit): Set[SootMethod] = ftpIcfg.getCalleesOfCallAt(n).toSet
    override def getStartPointsOf(m: SootMethod): Set[SUnit] = ftpIcfg.getStartPointsOf(m).toSet
    override def getMethodOf(n: SUnit): SootMethod = ftpIcfg.getMethodOf(n)
    override def getCallersOf(m: SootMethod): Set[SUnit] = ftpIcfg.getCallersOf(m).toSet
  }
  val flowFunctions: FlowFunctions[SUnit, Trackable, SootMethod] = new FlowFunctions[SUnit, Trackable, SootMethod] {
    override def getNormalFlowFunction(curr: SUnit, succ: SUnit): FlowFunction[Trackable] = new FlowFunction[Trackable] {
      override def computeTargets(t: Trackable): Set[Trackable] = {
        return ftpFFs.getNormalFlowFunction(curr, succ).computeTargets(t).toSet
      }
    }
    override def getCallFlowFunction(callStmt: SUnit, destinationMethod: SootMethod): FlowFunction[Trackable] = new FlowFunction[Trackable] {
      override def computeTargets(t: Trackable): Set[Trackable] = {
        return ftpFFs.getCallFlowFunction(callStmt, destinationMethod).computeTargets(t).toSet
      }
    }
    override def getReturnFlowFunction(callSite: Option[SUnit], calleeMethod: SootMethod, exitStmt: SUnit, returnSite: Option[SUnit]): FlowFunction[Trackable] = new FlowFunction[Trackable] {
      override def computeTargets(t: Trackable): Set[Trackable] = {
        return ftpFFs.getReturnFlowFunction(if (callSite.isDefined) callSite.get else null, calleeMethod, exitStmt, if (returnSite.isDefined) returnSite.get else null).computeTargets(t).toSet
      }
    }
    override def getCallToReturnFlowFunction(callSite: SUnit, returnSite: SUnit): FlowFunction[Trackable] = new FlowFunction[Trackable] {
      override def computeTargets(t: Trackable): Set[Trackable] = {
        return ftpFFs.getCallToReturnFlowFunction(callSite, returnSite).computeTargets(t).toSet
      }
    }
  }

  def getInitialSeeds: Map[SUnit, Set[Trackable]] = ftp.initialSeeds().map { case (n, seeds) => (n -> seeds.toSet) }.toMap
  def getFlowFunctions: FlowFunctions[SUnit, Trackable, SootMethod] = flowFunctions
  def getFollowReturnsPastSeeds: Boolean = true
  def getZeroValue: Trackable = ftp.zeroValue()

}
