package IFDS

import org.scalatest._

import cell._

class SimpleTestBiDiRANoOPAL extends FlatSpec with Matchers {
  
  {
    val helper = new BiDiTestHelper()
    var fwProblem = helper.happyPathFW
    var bwProblem = helper.happyPathBW
    var bidiSolver = new BiDiRAIFDSSolver[Node, String, Method](fwProblem, bwProblem, new HandlerPool(1))
    bidiSolver.solve
    "BiDi Happy path" should "use all forward flow functions" in {
      fwProblem.allFlowFunctionsUsed should be (true)
    }
    it should "use all backward flow functions" in {
      bwProblem.allFlowFunctionsUsed should be (true)
    }
  }
  {
    val helper = new BiDiTestHelper()
    val fwProblem = helper.unbalancedReturnsBothDirectionsFW
    val bwProblem = helper.unbalancedReturnsBothDirectionsBW
    val bidiSolver = new BiDiRAIFDSSolver[Node, String, Method](fwProblem, bwProblem, new HandlerPool(1))
    bidiSolver.solve
    "BiDi Unbalanced returns both directions" should "use all forward flow functions" in {
      fwProblem.allFlowFunctionsUsed should be (true)
    }
    it should "use all backward flow functions" in {
      bwProblem.allFlowFunctionsUsed should be (true)
    }
  }
  {
    val helper = new BiDiTestHelper()
    val fwProblem = helper.unbalancedReturnsNonMatchingCallSitesFW
    val bwProblem = helper.unbalancedReturnsNonMatchingCallSitesBW
    val bidiSolver = new BiDiRAIFDSSolver[Node, String, Method](fwProblem, bwProblem, new HandlerPool(1))
    bidiSolver.solve
    "BiDi Unbalanced returns non matching call sites" should "use all forward flow functions" in {
      fwProblem.allFlowFunctionsUsed should be (true)
    }
    it should "use all backward flow functions" in {
      bwProblem.allFlowFunctionsUsed should be (true)
    }
  }
  {
    val helper = new BiDiTestHelper()
    val fwProblem = helper.returnsOnlyOneDirectionAndStopsFW
    val bwProblem = helper.returnsOnlyOneDirectionAndStopsBW
    val bidiSolver = new BiDiRAIFDSSolver[Node, String, Method](fwProblem, bwProblem, new HandlerPool(1))
    bidiSolver.solve
    "BiDi Returns only one direction and stops" should "use all forward flow functions" in {
      fwProblem.allFlowFunctionsUsed should be (true)
    }
    it should "use all backward flow functions" in {
      bwProblem.allFlowFunctionsUsed should be (true)
    }
  }
  {
    val helper = new BiDiTestHelper()
    val fwProblem = helper.reuseSummaryFW
    val bwProblem = helper.reuseSummaryBW
    val bidiSolver = new BiDiRAIFDSSolver[Node, String, Method](fwProblem, bwProblem, new HandlerPool(1))
    bidiSolver.solve
    "BiDi reuse summary" should "use all forward flow functions" in {
      fwProblem.allFlowFunctionsUsed should be (true)
    }
    it should "use all backward flow functions" in {
      bwProblem.allFlowFunctionsUsed should be (true)
    }
  }
  {
    val helper = new BiDiTestHelper()
    val fwProblem = helper.dontResumeIfReturnFlowIsKilledFW
    val bwProblem = helper.dontResumeIfReturnFlowIsKilledBW
    val bidiSolver = new BiDiRAIFDSSolver[Node, String, Method](fwProblem, bwProblem, new HandlerPool(1))
    bidiSolver.solve
    "BiDi dont resume if return flow is killed" should "use all forward flow functions" in {
      fwProblem.allFlowFunctionsUsed should be (true)
    }
    it should "use all backward flow functions" in {
      bwProblem.allFlowFunctionsUsed should be (true)
    }
  }
}
