package taintanalysis

import flow.twist.config.AnalysisConfiguration
import flow.twist.config.AnalysisContext
import flow.twist.trackable.Trackable

import IFDS.IFDSSolver

import cell._
import lattice._

import soot.{Unit=>SUnit, SootMethod}

class RunRASEQSootSolver(config: AnalysisConfiguration, threads: Int) {
  val stp = new SootTabulationProblem(config, threads)
  def run(): Unit = {
    println(s"Starting new RA seq solver")
    val solver = new IFDSSolver[SUnit, Trackable, SootMethod](stp)
    solver.solve()
  }

  def tpContext(): AnalysisContext = stp.getContext()
}
