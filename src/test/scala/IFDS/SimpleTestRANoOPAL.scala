package IFDS

import org.scalatest._

import cell._

class SimpleTestRANoOPAL extends FlatSpec with Matchers {

  val helper = new TestHelper()

  "Not all flows used" should "not have used all flow functions" in {
    val ifdsProblem = helper.notAllFlowsUsed
    val solver: RAIFDSSolver[Node, String, Method] = new RAIFDSSolver(ifdsProblem, new HandlerPool(1))
    solver.solve
    ifdsProblem.allFlowFunctionsUsed should be (false)
  }
  // Because we are running in threads we cant use this test.
  // "Unexpected fact" should "throw exception" in {
  //   val ifdsProblem = helper.unexpectedFact
  //   val solver: RAIFDSSolver[Node, String, Method] = new RAIFDSSolver(ifdsProblem, new HandlerPool(1))
  //   assertThrows[AssertionError] {
  //     solver.solve
  //   }
  // }
  {
    val ifdsProblem = helper.happyPath
    val solver: RAIFDSSolver[Node, String, Method] = new RAIFDSSolver(ifdsProblem, new HandlerPool(1))
    solver.solve
    "Happy path no OPAL" should "use all flow functions" in {
      ifdsProblem.allFlowFunctionsUsed should be (true)
    }
    // it should "return results" in {
    //   //solver.results.isEmpty should be (false)
    // }
    // it should "have a non-empty supergraph" in {
    //   //solver.genSuperGraph().isEmpty should be (false)
    // }
  }
  "Merge flow after return site" should "use all flow functions" in {
    val ifdsProblem = helper.mergeFlowAfterReturnSite
    val solver: RAIFDSSolver[Node, String, Method] = new RAIFDSSolver(ifdsProblem, new HandlerPool(1))
    solver.solve
    ifdsProblem.allFlowFunctionsUsed should be (true)
  }
  "Reuse summary no OPAL" should "use all flow func2tions" in {
    val ifdsProblem = helper.reuseSummary
    val solver: RAIFDSSolver[Node, String, Method] = new RAIFDSSolver(ifdsProblem, new HandlerPool(1))
    solver.solve
    ifdsProblem.allFlowFunctionsUsed should be (true)
  }
  "Reuse summary for recursive call" should "use all flow functions" in {
    val ifdsProblem = helper.reuseSummaryForRecursiveCall
    val solver: RAIFDSSolver[Node, String, Method] = new RAIFDSSolver(ifdsProblem, new HandlerPool(1))
    solver.solve
    ifdsProblem.allFlowFunctionsUsed should be (true)
  }
  "Branch" should "use all flow functions" in {
    val ifdsProblem = helper.branch
    val solver: RAIFDSSolver[Node, String, Method] = new RAIFDSSolver(ifdsProblem, new HandlerPool(1))
    solver.solve
    ifdsProblem.allFlowFunctionsUsed should be (true)
  }
  "Unbalanced return" should "use all flow functions" in {
    val ifdsProblem = helper.unbalancedReturn
    val solver: RAIFDSSolver[Node, String, Method] = new RAIFDSSolver(ifdsProblem, new HandlerPool(1))
    solver.solve
    ifdsProblem.allFlowFunctionsUsed should be (true)
  }
  {
    val ifdsProblem = helper.artificalReturnEdgeForNoCallersCase
    val solver: RAIFDSSolver[Node, String, Method] = new RAIFDSSolver(ifdsProblem, new HandlerPool(1))
    solver.solve
    "Artifical return edge for no callers case" should "use all flow functions" in {
      ifdsProblem.allFlowFunctionsUsed should be (true)
    }
    // it should "return results" in {
    //   solver.results.isEmpty should be (false)
    // }
  }
}
