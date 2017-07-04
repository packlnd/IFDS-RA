package IFDS

import scala.collection.concurrent.TrieMap

import cell._
import lattice._

import scala.util.{Failure, Success}
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

// Arguments to the IFDS solver is an IFDS problem and a Reactive Async
// thread pool. The reason why the pool is provided as an argument is so
// the bidirectional solvers two internal solvers can use the same handler
// pool.
class RAIFDSSolver[N, D, M](ifdsProblem: IFDSProblem[N, D, M], pool: HandlerPool) {

  // PathEdgeKey used by RA cell
  object PathEdgeKey extends Key[Set[PathEdgeContainer[N,D]]] {

    // Resolve not actually used in current implementation because
    // there are no cyclic dependencies.
    def resolve[K <: Key[Set[PathEdgeContainer[N,D]]]](
      cells: Seq[Cell[K, Set[PathEdgeContainer[N,D]]]]
    ): Seq[(Cell[K, Set[PathEdgeContainer[N,D]]], Set[PathEdgeContainer[N,D]])] = {
      cells.map(cell => (cell, Set[PathEdgeContainer[N,D]]()))
    }
    def fallback[K <: Key[Set[PathEdgeContainer[N,D]]]](
      cells: Seq[Cell[K, Set[PathEdgeContainer[N,D]]]]
    ): Seq[(Cell[K, Set[PathEdgeContainer[N,D]]], Set[PathEdgeContainer[N,D]])] = {
      // Complete cell with empty set, to terminate algorithm
      cells.map(cell => (cell, Set[PathEdgeContainer[N,D]]()))
    }
    override def toString = "PathEdgeContainerKey"
  }

  // Lattice used by RA cell. Join and empty are set union and empty set.
  implicit object PathEdgeLattice extends Lattice[Set[PathEdgeContainer[N,D]]] {
    override def join(
      current: Set[PathEdgeContainer[N,D]],
      next: Set[PathEdgeContainer[N,D]]
    ): Set[PathEdgeContainer[N,D]] = {
      current ++ next
    }
    override val empty: Set[PathEdgeContainer[N,D]] = Set()
  }

  // One global cell which is used to propagate all path edges.
  val cc = CellCompleter[PathEdgeKey.type, Set[PathEdgeContainer[N,D]]](pool, PathEdgeKey)
  // Add an onNext callback which propagates path edges.
  cc.cell.onNext {
    case Success(edges) =>
      for (edge <- edges) {
        if (icfg.isCallStatement(edge.targetNode)) {
          pool.execute(() => processCall(edge))
        } else {
          if (icfg.isExitStatement(edge.targetNode)) {
            pool.execute(() => processExit(edge))
          }
          if (!icfg.getSuccessorsOf(edge.targetNode).isEmpty) {
            pool.execute(() => processNormalFlow(edge))
          }
        }
      }
    case Failure(e) => ???
  }
  val icfg: HerosICFG[N, M] = ifdsProblem.getIcfg
  val flowFunctions: FlowFunctions[N, D, M] = ifdsProblem.getFlowFunctions

  val zeroValue = ifdsProblem.getZeroValue
  val followReturnsPastSeeds = ifdsProblem.getFollowReturnsPastSeeds
  val initialSeeds = ifdsProblem.getInitialSeeds

  val endSummary: TrieMap[(N, D), TrieMap[(N, D), Unit]] = TrieMap()
  val incoming: TrieMap[(N, D), TrieMap[(N, D), Unit]] = TrieMap()

  def solve() = {
    pool.execute(() => submitInitialSeeds())
    val future = pool.quiescentResolveCell
    Await.ready(future, 15.minutes)
  }

  def submitInitialSeeds() = {
    for ((n, facts) <- initialSeeds)
      facts.foreach(d => propagate(zeroValue, n, d, None, false))
  }

  def propagate(
    sourceVal: D,
    target: N,
    targetVal: D,
    relatedCallSite: Option[N],
    isUnbalancedReturn: Boolean
  ): Unit = {
    val edge = PathEdgeContainer[N,D](sourceVal, target, targetVal)
    cc.putNext(Set(edge))
  }

  def propagateUnbalancedReturnFlow(retSiteC: N, targetVal: D, relatedCallSite: N): Unit = {
    propagate(zeroValue, retSiteC, targetVal, Some(relatedCallSite), true)
  }

  def processCall(edge: PathEdgeContainer[N,D]) = {
    val d1 = edge.sourceFact
    val d2 = edge.targetFact
    val n = edge.targetNode

    val returnSiteNs = icfg.getReturnSitesOfCallAt(n)
    for (sCalledProcN <- icfg.getCalleesOfCallAt(n)) {
      val ff = flowFunctions.getCallFlowFunction(n, sCalledProcN)
      val res = ff(d2)
      for (startPoint <- icfg.getStartPointsOf(sCalledProcN)) {
        for (d3 <- res) {
          incoming.putIfAbsent((startPoint, d3), TrieMap[(N,D), Unit]())
          // Get snapshot before propagate else exit node could get added to
          // endSummary (line 158) and processed both here and line 159-182
          val snapshot = endSummary.getOrElse((startPoint, d3), TrieMap()).snapshot()
          incoming((startPoint, d3)).putIfAbsent((n, d2), Unit)
          propagate(d3, startPoint, d3, Some(n), false)
          for (((ep, d4), _) <- snapshot) {
            for (returnSiteN <- returnSiteNs) {
              val ff = flowFunctions.getReturnFlowFunction(Some(n), sCalledProcN, ep, Some(returnSiteN))
              for (d5 <- ff(d4)) {
                val d5_restoredContext = restoreContextOnReturnedFact(n, d2, d5)
                propagate(d1, returnSiteN, d5_restoredContext, Some(n), false)
              }
            }
          }
        }
      }
    }
    // Line 17-19
    for (returnSiteN <- returnSiteNs) {
      val ff = flowFunctions.getCallToReturnFlowFunction(n, returnSiteN)
      for (d3 <- ff(d2)) {
        propagate(d1, returnSiteN, d3, Some(n), false)
      }
    }
  }

  // Lines 21-32
  def processExit(edge: PathEdgeContainer[N,D]) = {
    val d1 = edge.sourceFact
    val n = edge.targetNode
    val d2 = edge.targetFact

    val methodThatNeedsSummary = icfg.getMethodOf(n)
    // Do any of the start points for this method have entries in incoming? Assume no
    var entriesInIncomingForMethodSps = false
    for (startPoint <- icfg.getStartPointsOf(methodThatNeedsSummary)) {
      endSummary.putIfAbsent((startPoint, d1), TrieMap())
      val snapshot = incoming.getOrElse((startPoint, d1), TrieMap()).snapshot()
      endSummary((startPoint, d1)).putIfAbsent((n, d2), Unit)
      for (((c,d4), _) <- snapshot) {
        // Since we entered this for loop, incoming(sp, d1) is not empty
        // and at least one start point has an entry in incoming.
        entriesInIncomingForMethodSps = true
        for (retSiteC <- icfg.getReturnSitesOfCallAt(c)) {
          val ff = flowFunctions.getReturnFlowFunction(Some(c), methodThatNeedsSummary, n, Some(retSiteC))
          for (d5 <- ff(d2)) {
            // Looks like race condition because accessing cell results
            // However the edges we are looking for have already been added to result
            // as we are processing exit of incoming edge. If an edge is processed twice,
            // it would not be propagated twice because after putNext,
            // onNext is only called for a new result. However this should probably be
            // fixed if possible for (potential) performance reasons.
            for (edge <- cc.cell.getResult()) {
              if (edge.targetNode.equals(c) && edge.targetFact.equals(d4)) {
                val d3 = edge.sourceFact
                val d5_restoredContext = restoreContextOnReturnedFact(c, d4, d5)
                propagate(d3, retSiteC, d5_restoredContext, Some(c), false)
              }
            }
          }
        }
      }
    }
    if (followReturnsPastSeeds && !entriesInIncomingForMethodSps && d1.equals(zeroValue)) {
      // Unbalanced return
      val callers = icfg.getCallersOf(methodThatNeedsSummary)
      for (c <- callers) {
        for (retSiteC <- icfg.getReturnSitesOfCallAt(c)) {
          val ff = flowFunctions.getReturnFlowFunction(Some(c), methodThatNeedsSummary, n, Some(retSiteC))
          for (d5 <- ff(d2)) {
            propagateUnbalancedReturnFlow(retSiteC, d5, c)
          }
        }
      }
      if (callers.isEmpty) {
        val ff = flowFunctions.getReturnFlowFunction(None, methodThatNeedsSummary, n, None)
        ff(d2)
      }
    }
  }

  // Lines 33-37
  def processNormalFlow(edge: PathEdgeContainer[N, D]) = {
    val d1 = edge.sourceFact
    val d2 = edge.targetFact
    val n = edge.targetNode

    for (m <- icfg.getSuccessorsOf(n)) {
      val ff = flowFunctions.getNormalFlowFunction(n, m)
      for (d3 <- ff(d2)) {
        propagate(d1, m, d3, None, false)
      }
    }
  }

  // This is used by the APSA taint analysis exercise:
  // https://github.com/stg-tud/apsa
  def restoreContextOnReturnedFact(callSite: N, d4: D, d5: D): D = {
    return d5;
  }
}
