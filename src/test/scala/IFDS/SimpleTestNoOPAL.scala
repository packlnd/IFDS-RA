package IFDS

import org.scalatest._

case class Node(name: String) {}
case class Method(name: String) {}

// Used only internally as an edge flowfunction. Is converted to an
// IFDS compatible flow function in createFlowFunction in SimpleTestIFDSProblem line 26
case class FlowFunc(times: Int, source: String, targets: String*) {}

abstract class Edge { def flowFuncs: Set[FlowFunc] }
case class NormalEdge(n: Node, succN: Node, flowFuncs: Set[FlowFunc]) extends Edge
case class ReturnEdge(callSite: Option[Node], exit: Node, returnSite: Option[Node], calleeMethod: Method, flowFuncs: Set[FlowFunc]) extends Edge {}
case class CallEdge(callSite: Node, destinationMethod: Method, flowFuncs: Set[FlowFunc]) extends Edge {}
case class CallToReturnEdge(callSite: Node, returnSite: Node, flowFuncs: Set[FlowFunc]) extends Edge {}

class SimpleTestNoOPAL extends FlatSpec with Matchers {

  val helper = new TestHelper()

  "Not all flows used" should "not have used all flow functions" in {
    val ifdsProblem = helper.notAllFlowsUsed
    val solver: IFDSSolver[Node, String, Method] = new IFDSSolver(ifdsProblem)
    solver.solve
    ifdsProblem.allFlowFunctionsUsed should be (false)
  }
  "Unexpected fact" should "throw exception" in {
    val ifdsProblem = helper.unexpectedFact
    val solver: IFDSSolver[Node, String, Method] = new IFDSSolver(ifdsProblem)
    assertThrows[AssertionError] {
      solver.solve
    }
  }
  {
    val ifdsProblem = helper.happyPath
    val solver: IFDSSolver[Node, String, Method] = new IFDSSolver(ifdsProblem)
    solver.solve
    "Happy path no OPAL" should "use all flow functions" in {
      ifdsProblem.allFlowFunctionsUsed should be (true)
    }
    it should "return results" in {
      solver.results.isEmpty should be (false)
    }
    it should "have a non-empty supergraph" in {
      solver.genSuperGraph().isEmpty should be (false)
    }
  }
  "Reuse summary no OPAL" should "use all flow functions" in {
    val ifdsProblem = helper.reuseSummary
    val solver: IFDSSolver[Node, String, Method] = new IFDSSolver(ifdsProblem)
    solver.solve
    ifdsProblem.allFlowFunctionsUsed should be (true)
  }
  "Reuse summary for recursive call" should "use all flow functions" in {
    val ifdsProblem = helper.reuseSummaryForRecursiveCall
    val solver: IFDSSolver[Node, String, Method] = new IFDSSolver(ifdsProblem)
    solver.solve
    ifdsProblem.allFlowFunctionsUsed should be (true)
  }
  "Branch" should "use all flow functions" in {
    val ifdsProblem = helper.branch
    val solver: IFDSSolver[Node, String, Method] = new IFDSSolver(ifdsProblem)
    solver.solve
    ifdsProblem.allFlowFunctionsUsed should be (true)
  }
  "Unbalanced return" should "use all flow functions" in {
    val ifdsProblem = helper.unbalancedReturn
    val solver: IFDSSolver[Node, String, Method] = new IFDSSolver(ifdsProblem)
    solver.solve
    ifdsProblem.allFlowFunctionsUsed should be (true)
  }
  {
    val ifdsProblem = helper.artificalReturnEdgeForNoCallersCase
    val solver: IFDSSolver[Node, String, Method] = new IFDSSolver(ifdsProblem)
    solver.solve
    "Artifical return edge for no callers case" should "use all flow functions" in {
      ifdsProblem.allFlowFunctionsUsed should be (true)
    }
    it should "return results" in {
      solver.results.isEmpty should be (false)
    }
  }
}
