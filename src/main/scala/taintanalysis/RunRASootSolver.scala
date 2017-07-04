package taintanalysis

import flow.twist.config.AnalysisConfiguration
import flow.twist.config.AnalysisContext
import flow.twist.trackable.Trackable

import IFDS.RAIFDSSolver

import cell._
import lattice._

import soot.{Unit=>SUnit, SootMethod}

class RunRASootSolver(config: AnalysisConfiguration, threads: Int) {
  val stp = new SootTabulationProblem(config, threads)
  def run(): Unit = {
    println(s"Starting new RA solver with $threads threads")
    val pool = new HandlerPool(threads)
    val solver = new RAIFDSSolver[SUnit, Trackable, SootMethod](stp, pool)
    solver.solve()
  }

  def tpContext(): AnalysisContext = stp.getContext()
}
