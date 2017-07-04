package taintanalysis

import IFDS.{FlowFunctions, FlowFunction, Propagator, KillGenInfo}

import org.opalj.br.Method

import scala.collection.mutable.{Set => MSet}

class TaintAnalysisFlowFunctions(propagators: List[List[Propagator]]) extends FlowFunctions[MStmt, Fact, Method] {

  def addPropagatedTaints(f: Fact, propagatedTaints: MSet[Fact], edge: Edge): Boolean = {
    var killed: Boolean = false
    for (phase <- propagators) {
      for (propagator <- phase.filter(_.canHandle(f))) {
        val visitor = new PropagatorVisitor(propagator, f)
        val killGen = edge.accept[KillGenInfo](visitor)
        killGen.gen.foreach(propagatedTaints.add(_))
        killed |= killGen.killed
      }
      if (killed) {
        return killed
      }
    }
    return killed
  }

  def propagate(edge: Edge): FlowFunction[Fact] = new FlowFunction[Fact] {
    override def computeTargets(f: Fact): Set[Fact] = {
      val propagatedTaints: MSet[Fact] = MSet()
      if (!addPropagatedTaints(f, propagatedTaints, edge)) {
        propagatedTaints.add(edge.accept[Fact](new Visitor[Fact] {
          override def visit(ctrEdge: CallToReturnEdge): Fact = {f.createAlias(ctrEdge.callSite);}
          override def visit(retEdge: ReturnEdge): Fact = {throw new IllegalStateException();}
          override def visit(calEdge: CallEdge): Fact = {throw new IllegalStateException();}
          override def visit(norEdge: NormalEdge): Fact = {f.createAlias(norEdge.curr);}
        }))
      }
      propagatedTaints.toSet
    }
  }

  def getNormalFlowFunction(curr: MStmt, succ: MStmt): FlowFunction[Fact] = {
    return propagate(new NormalEdge(curr, succ))
  }

  def getCallFlowFunction(callStmt: MStmt, destinationMethod: Method): FlowFunction[Fact] = {
    return propagate(new CallEdge(callStmt, destinationMethod))
  }

  def getReturnFlowFunction(
    callSite: Option[MStmt],
    calleeMethod: Method,
    exitStmt: MStmt,
    returnSite: Option[MStmt]
  ): FlowFunction[Fact] = {
    return propagate(new ReturnEdge(callSite, calleeMethod, exitStmt, returnSite))
  }

  def getCallToReturnFlowFunction(callSite: MStmt, returnSite: MStmt): FlowFunction[Fact] = {
    return propagate(new CallToReturnEdge(callSite, returnSite))
  }
}

class PropagatorVisitor(propagator: Propagator, f: Fact) extends Visitor[KillGenInfo] {
  override def visit(edge: CallToReturnEdge): KillGenInfo = {
    propagator.propagateCallToReturnFlow(f, edge.callSite)
  }
  override def visit(edge: CallEdge): KillGenInfo = {
    propagator.propagateCallFlow(f, edge.callStmt, edge.destinationMethod)
  }
  override def visit(edge: ReturnEdge): KillGenInfo = {
    propagator.propagateReturnFlow(f, edge.callSite, edge.calleeMethod, edge.exitStmt, edge.returnSite)
  }
  override def visit(edge: NormalEdge): KillGenInfo = {
    propagator.propagateNormalFlow(f, edge.curr, edge.succ)
  }
}
