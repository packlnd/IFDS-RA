package taintanalysis

import org.opalj.br.Method

import scala.collection.mutable.{Set => MSet}

import IFDS.{IFDSProblem, FlowFunctions, Propagator, HerosICFG}
import propagator.{PrimitiveTaintKiller, JavaUtilKiller, SpecificMethodKiller}
import propagator.forwards

class TabulationProblem(direction: Direction.Value, seeds: MSet[MStmt], icfg: HerosICFG[MStmt, Method]) extends IFDSProblem[MStmt, Fact, Method] {
  val transitiveSinkCaller = new TransitiveSinkCaller();
  val propagators: List[List[Propagator]] = List(
    List(
    //   new PrimitiveTaintKiller(),
    //   new JavaUtilKiller(context)
    ),
    List(
    //   new forwards.StringBuilderPropagator(),
    //   new forwards.ShortcutPropagator(),
    //   new forwards.PermissionCheckPropagator(),
    //   new forwards.PropagateOverTarget(context)
    ),
    List(
    //   new ZeroAtParameterHandler(context),
    //   new flow.twist.propagator.forwards.DefaultTaintPropagator(context),
    //   new PayloadSourceRecognizer(context),
    //   new ClassInstantiationPropagator()
    )
  )

  val flowFunctions: FlowFunctions[MStmt, Fact, Method] = new TaintAnalysisFlowFunctions(propagators)

  def getInitialSeeds: Map[MStmt, Set[Fact]] = seeds.map(_ -> Set(getZeroValue)).toMap
  def getFlowFunctions: FlowFunctions[MStmt, Fact, Method] = flowFunctions
  def getIcfg: HerosICFG[MStmt, Method] = icfg
  def getFollowReturnsPastSeeds: Boolean = true
  def getZeroValue: Fact = Zero.ZERO

}
