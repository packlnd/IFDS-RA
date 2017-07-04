package IFDS

import scala.collection.mutable.{ListBuffer, Queue, Set, Map}

import heros.solver.{IFDSSolver => HerosSolver}

/* An IFDS solver based on an algorithm in the paper "Practical
 * Extensions to the IFDS Algorithm" by Naeem et al. [1]:
 * http://link.springer.com/chapter/10.1007/978-3-642-11970-5_8
 */

// Equivalent of <sourceNode, sourceFact> -> <targetNode, targetFact>
case class PathEdgeContainer[N, D](sourceFact: D, targetNode: N, targetFact: D) {override def productPrefix = "Edge"}

/**
  * @tparam N type of node in the supergraph
  * @tparam M type of method/procedure
  * @tparam D type of facts
  */
class IFDSSolver[N, D, M](ifdsProblem: IFDSProblem[N, D, M]) {

  // Lines 1-4 (Fig. 2 of [1])
  val pathEdge: Set[PathEdgeContainer[N,D]] = Set()
  val workList: Queue[PathEdgeContainer[N,D]] = Queue()

  val icfg: HerosICFG[N, M] = ifdsProblem.getIcfg
  val flowFunctions: FlowFunctions[N, D, M] = ifdsProblem.getFlowFunctions

  val zeroValue = ifdsProblem.getZeroValue
  val followReturnsPastSeeds = ifdsProblem.getFollowReturnsPastSeeds
  val initialSeeds = ifdsProblem.getInitialSeeds

  /* According to Fig. 4 (extended IFDS algorithm):
   *   (e_p, d_4) \in EndSummary.get((s_p, d_3))
   *
   * Thus:
   * Given a pair (s_p, d_3), we can look up a set of pairs in `EndSummary`.
   */
  val endSummary: Map[(N, D), Set[(N, D)]] = Map()

  /*
   * The same with Incoming
   */
  val incoming: Map[(N, D), Set[(N, D)]] = Map()

  val sNode: Map[N, Set[D]] = Map()

  def solve() = {
    submitInitialSeeds()
    forwardTabulateSLRPs() // Line 5
    // println("Size of path edge: " + pathEdge.size)
  }

  def submitInitialSeeds() = {
    // This is just populating workList and pathEdge
    // equals line 2-3, fig. 4 [1]
    for ((n, facts) <- initialSeeds)
      facts.foreach(d => propagate(zeroValue, n, d, None, false))
  }

  def results(): ListBuffer[Set[D]] = {
    val ret: ListBuffer[Set[D]] = ListBuffer()
    for(n <- sNode.keySet) {
      ret += resultsAt(n)
    }
    ret
  }

  def genSuperGraph() = {
    sNode
  }

  //Lines 6-8, fix the iteration problem, not tested yet
  def resultsAt(node: N): Set[D] = {
    val results: Set[D] = Set().empty
    if (pathEdge.nonEmpty) {
      for (d2 <- sNode(node)) {
        val procOfN = icfg.getMethodOf(node)
        for (sp <- icfg.getStartPointsOf(procOfN)) {
          for (d1 <- sNode.getOrElse(sp, Set())) {
            if (pathEdge.contains(PathEdgeContainer(d1, node, d2))){
              results.add(d2)
            }
          }
        }
      }
    }
    results
  }

  // Lines 10-39f
  def forwardTabulateSLRPs() = {
    while (!workList.isEmpty) {
      val edge = workList.dequeue
      if (icfg.isCallStatement(edge.targetNode)) {
        processCall(edge)
      } else {
        // From Heros (IDESolver.java:896):
        //  note that some statements, such as "throw"
        //  may be both an exit statement and a "normal" statement

        // Lines 21-32
        if (icfg.isExitStatement(edge.targetNode)) {
          processExit(edge)
        }
        // Lines 33-37
        if (!icfg.getSuccessorsOf(edge.targetNode).isEmpty) {
          processNormalFlow(edge)
        }
      }
    }
  }

  // Lines 9
  def propagate(
    sourceVal: D,
    target: N,
    targetVal: D,
    relatedCallSite: Option[N],
    isUnbalancedReturn: Boolean
  ): Unit = {
    val edge = PathEdgeContainer[N,D](sourceVal, target, targetVal)
    if (!pathEdge.contains(edge)) {
      pathEdge.add(edge)
      workList += edge
      //genSuperGraph
      if (!sNode.contains(edge.targetNode))
        sNode.update(edge.targetNode, Set())
      sNode(edge.targetNode).+=(edge.targetFact)
    }
  }

  def propagateUnbalancedReturnFlow(retSiteC: N, targetVal: D, relatedCallSite: N): Unit = {
    propagate(zeroValue, retSiteC, targetVal, Some(relatedCallSite), true)
  }

  // Lines 13-20
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
          propagate(d3, startPoint, d3, Some(n), false)
          if (!incoming.contains((startPoint, d3)))
            incoming.update((startPoint, d3), Set())
          incoming((startPoint, d3)).add((n, d2))
          for ((ep, d4) <- endSummary.getOrElse((startPoint, d3), Set())) {
            for (returnSiteN <- returnSiteNs) {
              val ff = flowFunctions.getReturnFlowFunction(Some(n), sCalledProcN, ep, Some(returnSiteN))
              for (d5 <- ff(d4)) {
                // This propagate isn't actually in the pseudo code of [1]
                // but is in the Heros implementation IDESolver:396
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
      if (!endSummary.contains((startPoint, d1)))
        endSummary.update((startPoint, d1), Set())
      endSummary((startPoint, d1)).add((n, d2))

      for ((c,d4) <- incoming.getOrElse((startPoint, d1), Set())) {
        // Since we entered this for loop, incoming(sp, d1) is not empty
        // and at least one start point has an entry in incoming.
        entriesInIncomingForMethodSps = true
        for (retSiteC <- icfg.getReturnSitesOfCallAt(c)) {
          val ff = flowFunctions.getReturnFlowFunction(Some(c), methodThatNeedsSummary, n, Some(retSiteC))
          for (d5 <- ff(d2)) {
            for (edge <- pathEdge) {
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

  def restoreContextOnReturnedFact(callSite: N, d4: D, d5: D): D = {
    return d5;
  }
}


