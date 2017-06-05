package IFDS

import scala.collection.concurrent.TrieMap

import cell._
import lattice._

import scala.util.{Failure, Success}
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class RAIFDSSolver[N, D, M](ifdsProblem: IFDSProblem[N, D, M], pool: HandlerPool) {
  object PathEdgeKey extends Key[Set[PathEdgeContainer[N,D]]] {
    def resolve[K <: Key[Set[PathEdgeContainer[N,D]]]](
      cells: Seq[Cell[K, Set[PathEdgeContainer[N,D]]]]
    ): Seq[(Cell[K, Set[PathEdgeContainer[N,D]]], Set[PathEdgeContainer[N,D]])] = {
      cells.map(cell => (cell, Set[PathEdgeContainer[N,D]]()))
    }
    def fallback[K <: Key[Set[PathEdgeContainer[N,D]]]](
      cells: Seq[Cell[K, Set[PathEdgeContainer[N,D]]]]
    ): Seq[(Cell[K, Set[PathEdgeContainer[N,D]]], Set[PathEdgeContainer[N,D]])] = {
      cells.map(cell => (cell, Set[PathEdgeContainer[N,D]]()))
    }
    override def toString = "PathEdgeContainerKey"
  }

  implicit object PathEdgeLattice extends Lattice[Set[PathEdgeContainer[N,D]]] {
    override def join(
      current: Set[PathEdgeContainer[N,D]],
      next: Set[PathEdgeContainer[N,D]]
    ): Set[PathEdgeContainer[N,D]] = {
      current ++ next
    }
    override val empty: Set[PathEdgeContainer[N,D]] = Set()
  }

  val cc = CellCompleter[PathEdgeKey.type, Set[PathEdgeContainer[N,D]]](pool, PathEdgeKey)
  cc.cell.onNext {
    case Success(edges) =>
      for (edge <- edges) {
        //println(s"Processing $edge")
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
    //println(cc.cell.getResult().size)
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
    //println(s"Call $edge")
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
          //println(s"Propagating call ($d3, $startPoint, $d3)")
          propagate(d3, startPoint, d3, Some(n), false)
          for (((ep, d4), _) <- snapshot) {
            for (returnSiteN <- returnSiteNs) {
              val ff = flowFunctions.getReturnFlowFunction(Some(n), sCalledProcN, ep, Some(returnSiteN))
              for (d5 <- ff(d4)) {
                val d5_restoredContext = restoreContextOnReturnedFact(n, d2, d5)
                //println(s"Propagating return flow endSummary ($d1, $returnSiteN, $d5_restoredContext)")
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
        //println(s"Propagating call to return flow ($d1, $returnSiteN, $d3)")
        propagate(d1, returnSiteN, d3, Some(n), false)
      }
    }
  }

  // Lines 21-32
  def processExit(edge: PathEdgeContainer[N,D]) = {
    //println(s"Exit $edge")
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
                //println(s"Propagating return flow ($d3, $retSiteC, $d5_restoredContext)")
                propagate(d3, retSiteC, d5_restoredContext, Some(c), false)
              }
            }
          }
        }
      }
    }
    if (followReturnsPastSeeds && !entriesInIncomingForMethodSps && d1.equals(zeroValue)) {
      val callers = icfg.getCallersOf(methodThatNeedsSummary)
      for (c <- callers) {
        for (retSiteC <- icfg.getReturnSitesOfCallAt(c)) {
          val ff = flowFunctions.getReturnFlowFunction(Some(c), methodThatNeedsSummary, n, Some(retSiteC))
          for (d5 <- ff(d2)) {
            //println(s"Propagating unbalanced return flow ($zeroValue, $retSiteC, $d5)")
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
    //println(s"Normal $edge")
    val d1 = edge.sourceFact
    val d2 = edge.targetFact
    val n = edge.targetNode

    for (m <- icfg.getSuccessorsOf(n)) {
      val ff = flowFunctions.getNormalFlowFunction(n, m)
      for (d3 <- ff(d2)) {
        //println(s"Propagating normal flow ($d1, $m, $d3)")
        propagate(d1, m, d3, None, false)
      }
    }
  }

  def restoreContextOnReturnedFact(callSite: N, d4: D, d5: D): D = {
    return d5;
  }
}
