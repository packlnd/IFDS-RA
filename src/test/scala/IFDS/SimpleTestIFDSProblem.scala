package IFDS

import scala.collection.mutable.ListBuffer

class SimpleIFDSProblem(
  val methodToStartPoints: Map[Method, Set[Node]],
  val normalEdges: Set[NormalEdge],
  val returnEdges: Set[ReturnEdge],
  val callEdges: Set[CallEdge],
  val callToReturnEdges: Set[CallToReturnEdge],
  val stmtToMethod: Map[Node, Method],
  val initialSeeds: Set[Node],
  val followReturnsPastSeeds: Boolean
) extends IFDSProblem[Node, String, Method] {

  // This is borrowed from Heros, has to do with number of times a flow function can be used.
  val remainingFlowFunctions: ListBuffer[FlowFunc] = ListBuffer()

  def insertFlowFuncs(edges: Set[_ <: Edge]) = {
    edges.foreach(_.flowFuncs.foreach(ff => (1 to ff.times).foreach(_ => remainingFlowFunctions.append(ff))))
  }

  insertFlowFuncs(normalEdges)
  insertFlowFuncs(returnEdges)
  insertFlowFuncs(callEdges)
  insertFlowFuncs(callToReturnEdges)

  def allFlowFunctionsUsed: Boolean = {
    remainingFlowFunctions.isEmpty
  }

  def getZeroValue: String = "0"
  def getFollowReturnsPastSeeds: Boolean = followReturnsPastSeeds
  def getInitialSeeds: Map[Node, Set[String]] = initialSeeds.map(_ -> Set(getZeroValue)).toMap

  def getIcfg(): HerosICFG[Node, Method] = new HerosICFG[Node, Method] {
    def getCalleesOfCallAt(n: Node): Set[Method] = callEdges.filter(_.callSite.equals(n)).map(_.destinationMethod)
    def getReturnSitesOfCallAt(n: Node): Set[Node] = {
      val ret = returnEdges.filter(e => e.callSite.isDefined && e.returnSite.isDefined && e.callSite.get.equals(n)).map(_.returnSite.get)
      val cal = callToReturnEdges.filter(_.callSite.equals(n)).map(_.returnSite)
      ret ++ cal
    }
    def getStartPointsOf(m: Method): Set[Node] = methodToStartPoints.getOrElse(m, Set())
    def getSuccessorsOf(n: Node): Set[Node] = normalEdges.filter(_.n.equals(n)).map(_.succN)
    def isCallStatement(n: Node): Boolean = callEdges.exists(_.callSite.equals(n))
    def isExitStatement(n: Node): Boolean = returnEdges.exists(_.exit.equals(n))
    def getCallersOf(m: IFDS.Method): Set[IFDS.Node] = {
      val ret = returnEdges.filter(e =>  e.callSite.isDefined && e.calleeMethod.equals(m)).map(_.callSite.get)
      val cal = callEdges.filter(_.destinationMethod.equals(m)).map(_.callSite)
      ret ++ cal
    }
    def getMethodOf(n: IFDS.Node): IFDS.Method = stmtToMethod(n)
  }

  def getFlowFunctions(): FlowFunctions[Node, String, Method] = new FlowFunctions[Node, String, Method] {
    def createFlowFunction(edge: Edge): FlowFunction[String] = new FlowFunction[String] {
      override def computeTargets(d: String): Set[String] = {
        for (ff <- edge.flowFuncs) {
          if (ff.source.equals(d)) {
            if (remainingFlowFunctions.contains(ff)) {
              // print(s"using $ff ")
              remainingFlowFunctions -= ff
              // println(s"${remainingFlowFunctions.size} flowfunctions remaining")
              return ff.targets.toSet
            } else {
              throw new AssertionError(s"Flow function $ff used too many times")
            }
          }
        }
        throw new AssertionError(s"Unexpected fact: $d");
      }
    }
    def getCallFlowFunction(callStmt: Node, destinationMethod: Method): FlowFunction[String] = {
      for (edge <- callEdges) {
        if (edge.callSite.equals(callStmt) && edge.destinationMethod.equals(destinationMethod)) {
          return createFlowFunction(edge)
        }
      }
      throw new AssertionError(s"No callflow function between callStmt: $callStmt and destinationMethod: $destinationMethod")
    }
    def getCallToReturnFlowFunction(callSite: Node, returnSite: Node): FlowFunction[String] = {
      for (edge <- callToReturnEdges) {
        if (edge.callSite.equals(callSite) && edge.returnSite.equals(returnSite)) {
          return createFlowFunction(edge)
        }
      }
      throw new AssertionError(s"No calltoreturnflow function between callSite: $callSite and returnSite: $returnSite")
    }
    def getNormalFlowFunction(curr: Node, succ: Node): FlowFunction[String] = {
      for (edge <- normalEdges) {
        if (edge.n.equals(curr) && edge.succN.equals(succ))
          return createFlowFunction(edge)
      }
      throw new AssertionError(s"No normalflow function between $curr and $succ")
    }
    def getReturnFlowFunction(callSite: Option[Node], calleeMethod: Method, exitStmt: Node, returnSite: Option[Node]): FlowFunction[String] = {
      for (edge <- returnEdges) {
          if (
              callSite.equals(edge.callSite)
              && edge.calleeMethod.equals(calleeMethod)
              && edge.exit.equals(exitStmt)
              && edge.returnSite.equals(returnSite)
            ) {
            return createFlowFunction(edge);
          }
        }
        throw new AssertionError(s"No returnflow function between callSite: $callSite, calleeMethod: $calleeMethod, exitStmt: $exitStmt, and returnSite: $returnSite")
    }
  }
}
