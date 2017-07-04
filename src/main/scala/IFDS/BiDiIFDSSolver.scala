package IFDS

import scala.collection.mutable.{Map => MMap, Set => MSet, ListBuffer}

class BiDiIFDSSolver[N, D, M](fwProblem: IFDSProblem[N, D, M], bwProblem: IFDSProblem[N, D, M]) {

  val fwSolver = new PausableIFDSSolver(new AugmentedIFDSProblem(fwProblem), "FW")
  val bwSolver = new PausableIFDSSolver(new AugmentedIFDSProblem(bwProblem), "BW")
  fwSolver.otherSolver = bwSolver
  bwSolver.otherSolver = fwSolver

  def solve = {
    bwSolver.submitInitialSeeds
    fwSolver.submitInitialSeeds
    while (!(fwSolver.isDone && bwSolver.isDone)) {
      bwSolver.forwardTabulateSLRPs()
      fwSolver.forwardTabulateSLRPs()
    }
  }

  def forwardResults: ListBuffer[MSet[D]] = {
    fwSolver.results.map(s => s.map(_.fact)) 
  }

  def backwardResults: ListBuffer[MSet[D]] = {
    bwSolver.results.map(s => s.map(_.fact))
  }

  case class AbstractionWithSource(source: Option[N], fact: D) {override def productPrefix = "AWS"}
  case class PausedEdge(retSiteC: N, target: AbstractionWithSource, relatedCallSite: N)
  case class LeakKey(source: Option[N], relatedCallSite: N)
  class PausableIFDSSolver(ifdsProblem: IFDSProblem[N, AbstractionWithSource, M], debug: String) extends IFDSSolver[N, AbstractionWithSource, M](ifdsProblem) {
    var otherSolver: PausableIFDSSolver = _
    val leakedSources: MSet[LeakKey] = MSet()
    val pausedEdges: MMap[LeakKey, MSet[PausedEdge]] = MMap()

    def isDone: Boolean = workList.isEmpty

    override def restoreContextOnReturnedFact(callSite: N, d4: AbstractionWithSource, d5: AbstractionWithSource): AbstractionWithSource = {
      AbstractionWithSource(d4.source, d5.fact)
    }

    override def propagateUnbalancedReturnFlow(retSiteC: N, targetVal: AbstractionWithSource, relatedCallSite: N): Unit = {
      val source = targetVal.source
      val leakKey = LeakKey(source, relatedCallSite)
      leakedSources.add(leakKey)
      if (otherSolver.hasLeaked(leakKey)) {
        otherSolver.unpausePathEdgesForSource(leakKey)
        super.propagateUnbalancedReturnFlow(retSiteC, targetVal, relatedCallSite)
        return
      }
      val existingPausedEdges = pausedEdges.getOrElseUpdate(leakKey, MSet())
      val edge = PausedEdge(retSiteC, targetVal, relatedCallSite)
      existingPausedEdges.add(edge)
      if (otherSolver.hasLeaked(leakKey) && existingPausedEdges.remove(edge)) {
        super.propagateUnbalancedReturnFlow(retSiteC, targetVal, relatedCallSite)
      }
    }

    override def propagate(
      sourceVal: AbstractionWithSource,
      target: N,
      targetVal: AbstractionWithSource,
      relatedCallSite: Option[N],
      isUnbalancedReturn: Boolean
    ): Unit = {
      if (isUnbalancedReturn) {
        assert(!sourceVal.source.isDefined)
        val newTargetVal = AbstractionWithSource(relatedCallSite, targetVal.fact)
        super.propagate(sourceVal, target, newTargetVal, relatedCallSite, isUnbalancedReturn)
      } else {
        super.propagate(sourceVal, target, targetVal, relatedCallSite, isUnbalancedReturn)
      }
    }

    def hasLeaked(leakKey: LeakKey): Boolean = {
      return leakedSources.contains(leakKey)
    }

    def unpausePathEdgesForSource(leakKey: LeakKey): Unit = {
      if (pausedEdges.contains(leakKey)) {
        for (edge <- pausedEdges(leakKey)) {
          pausedEdges(leakKey).remove(edge)
          super.propagateUnbalancedReturnFlow(edge.retSiteC, edge.target, edge.relatedCallSite)
        }
      }
    }
  }
  class AugmentedIFDSProblem(problem: IFDSProblem[N, D, M]) extends IFDSProblem[N, AbstractionWithSource, M] {
    val zero = AbstractionWithSource(None, problem.getZeroValue)
    val originalFunctions = problem.getFlowFunctions

    override def getFlowFunctions: FlowFunctions[N, AbstractionWithSource, M] = new FlowFunctions[N, AbstractionWithSource, M] {
      def getCallFlowFunction(callStmt: N, destinationMethod: M): FlowFunction[AbstractionWithSource] = new FlowFunction[AbstractionWithSource] {
        override def computeTargets(aws: AbstractionWithSource): Set[AbstractionWithSource] = {
          originalFunctions.getCallFlowFunction(callStmt, destinationMethod).computeTargets(aws.fact).map(AbstractionWithSource(None, _))
        }
      }
      def getCallToReturnFlowFunction(callSite: N, returnSite: N): FlowFunction[AbstractionWithSource] = new FlowFunction[AbstractionWithSource] {
        override def computeTargets(aws: AbstractionWithSource): Set[AbstractionWithSource] = {
          originalFunctions.getCallToReturnFlowFunction(callSite, returnSite).computeTargets(aws.fact).map(AbstractionWithSource(aws.source, _))
        }
      }
      def getNormalFlowFunction(curr: N, succ: N): FlowFunction[AbstractionWithSource] = new FlowFunction[AbstractionWithSource] {
        override def computeTargets(aws: AbstractionWithSource): Set[AbstractionWithSource] = {
          originalFunctions.getNormalFlowFunction(curr, succ).computeTargets(aws.fact).map(AbstractionWithSource(aws.source, _))
        }
      }
      def getReturnFlowFunction(callSite: Option[N], calleeMethod: M, exitStmt: N, returnSite: Option[N]): FlowFunction[AbstractionWithSource] = new FlowFunction[AbstractionWithSource] {
        override def computeTargets(aws: AbstractionWithSource): Set[AbstractionWithSource] = {
          originalFunctions.getReturnFlowFunction(callSite, calleeMethod, exitStmt, returnSite).computeTargets(aws.fact).map(AbstractionWithSource(aws.source, _))
        }
      }
    }

    override def getInitialSeeds: Map[N, Set[AbstractionWithSource]] = {
      problem.getInitialSeeds map { case (k,v) => (k, v.map(AbstractionWithSource(Some(k), _))) }
    }

    override def getIcfg: HerosICFG[N, M] = problem.getIcfg
    override def getZeroValue: AbstractionWithSource = zero
    override def getFollowReturnsPastSeeds: Boolean = problem.getFollowReturnsPastSeeds
  }
}
