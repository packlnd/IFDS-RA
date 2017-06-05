package IFDS

import scala.collection.concurrent.TrieMap

import cell._
import lattice._

import scala.util.{Failure, Success}
import scala.concurrent.{Await}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class BiDiRAIFDSSolver[N, D, M](
  fwProblem: IFDSProblem[N, D, M],
  bwProblem: IFDSProblem[N, D, M],
  pool: HandlerPool
) {

  val fwSolver = new PausableRAIFDSSolver(new AugmentedIFDSProblem(fwProblem), pool, "FW")
  val bwSolver = new PausableRAIFDSSolver(new AugmentedIFDSProblem(bwProblem), pool, "BW")
  fwSolver.otherSolver = bwSolver
  bwSolver.otherSolver = fwSolver

  def solve = {
    pool.execute(() => bwSolver.submitInitialSeeds )
    pool.execute(() => fwSolver.submitInitialSeeds )
    val future = pool.quiescentResolveCell
    Await.ready(future, 15.minutes)
  }

  case class AbstractionWithSource(source: Option[N], fact: D) {override def productPrefix = "AWS"}
  case class PausedEdge(retSiteC: N, target: AbstractionWithSource, relatedCallSite: N)
  case class LeakKey(source: Option[N], relatedCallSite: N)
  class PausableRAIFDSSolver(
    ifdsProblem: IFDSProblem[N, AbstractionWithSource, M],
    pool: HandlerPool,
    debug: String
  ) extends RAIFDSSolver[N, AbstractionWithSource, M](ifdsProblem, pool) {
    var otherSolver: PausableRAIFDSSolver = _
    val leakedSources: TrieMap[LeakKey, Unit] = TrieMap()
    val pausedEdges: TrieMap[LeakKey, TrieMap[PausedEdge, Unit]] = TrieMap()

    override def restoreContextOnReturnedFact(callSite: N, d4: AbstractionWithSource, d5: AbstractionWithSource): AbstractionWithSource = {
      AbstractionWithSource(d4.source, d5.fact)
    }

    override def propagateUnbalancedReturnFlow(
      retSiteC: N,
      targetVal: AbstractionWithSource,
      relatedCallSite: N
    ): Unit = {
      val source = targetVal.source
      val leakKey = LeakKey(source, relatedCallSite)
      leakedSources.putIfAbsent(leakKey, Unit)
      if (otherSolver.hasLeaked(leakKey)) {
        otherSolver.unpausePathEdgesForSource(leakKey)
        super.propagateUnbalancedReturnFlow(retSiteC, targetVal, relatedCallSite)
        return
      }
      pausedEdges.putIfAbsent(leakKey, TrieMap())
      pausedEdges.get(leakKey) match {
        case Some(existingPausedEdges) =>
          val edge = PausedEdge(retSiteC, targetVal, relatedCallSite)
          existingPausedEdges.putIfAbsent(edge, Unit)
          if (otherSolver.hasLeaked(leakKey) && existingPausedEdges.remove(edge, Unit)) {
            super.propagateUnbalancedReturnFlow(retSiteC, targetVal, relatedCallSite)
          }
        case None => ???
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
      val edges = pausedEdges.getOrElse(leakKey, TrieMap())
      for ((edge, u) <- edges) {
        if (edges.remove(edge, u))
          super.propagateUnbalancedReturnFlow(edge.retSiteC, edge.target, edge.relatedCallSite)
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
