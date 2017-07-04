package taintanalysis

import flow.twist.config.AnalysisConfiguration
import flow.twist.config.AnalysisContext
import flow.twist.trackable.Trackable

import IFDS.BiDiRAIFDSSolver

import cell._
import lattice._

import soot.{Unit=>SUnit, SootMethod}

class RunBiDiRASootSolver(fwConfig: AnalysisConfiguration, bwConfig: AnalysisConfiguration, threads: Int) {
  val bwStp = new SootTabulationProblem(bwConfig, threads)
  val fwStp = new SootTabulationProblem(fwConfig, threads)
  def run(): Unit = {
    println(s"Starting new RA solver with $threads threads")
    val pool = new HandlerPool(threads)
    val solver = new BiDiRAIFDSSolver[SUnit, Trackable, SootMethod](fwStp, bwStp, pool)
    solver.solve
  }

  def forwardsContext(): AnalysisContext = fwStp.getContext()
  def backwardsContext(): AnalysisContext = bwStp.getContext()
}
