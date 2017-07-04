package taintanalysis

import scala.collection.mutable.{Set => MSet}

import org.opalj.br.analyses.Project
import org.opalj.br.Method
import java.io.File

import org.opalj.log.OPALLogger
import org.opalj.fpcf.analysis.cg.cha.CHACallGraphKey
import org.opalj.AnalysisMode
import org.opalj.ai.analyses.cg.ComputedCallGraph
import org.opalj.ai.analyses.cg.VTACallGraphKey
import org.opalj.br.instructions.INVOKESTATIC
import org.opalj.log.DefaultLogContext
import org.opalj.log.ConsoleOPALLogger
import org.opalj.br.ClassFile
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config

import org.opalj.tac._
import org.opalj.util.PerformanceEvaluation.time

import IFDS.RAIFDSSolver
import IFDS.HerosICFG

import util.Util

import cell._

class TaintAnalysis {
  def executeAnalysis(threads: Int, rtPath: String): Unit ={
    val AnalysisModeKey = AnalysisMode.ConfigKey
    val analysisModeSpecification = s"$AnalysisModeKey = library with open packages assumption"
    val analysisModeConfig = ConfigFactory.parseString(analysisModeSpecification)
    val logContext = new DefaultLogContext
    OPALLogger.register(logContext,new ConsoleOPALLogger)
    val p = Project(
      new File(rtPath),
      logContext,
      analysisModeConfig.withFallback(ConfigFactory.load())
    )
    val ComputedCallGraph(callGraph, _, _) = p.get(CHACallGraphKey)
    val icfg = new TACOpalICFG(callGraph)
    val seeds = getSeeds(p, threads)
    println(s"Starting RA solver on $rtPath with $threads threads")
    time {
      val solver = new RAIFDSSolver(new TabulationProblem(Direction.FORWARDS, seeds, icfg), new HandlerPool(threads))
      solver.solve()
    }(t => println(s"Analysis time: ${t.toSeconds}"))
  }

  def getSeeds(p: Project[_], threads: Int): MSet[MStmt] = {
    val seeds: MSet[MStmt] = MSet()
    p.parForeachMethodWithBody(parallelizationLevel=threads) { mi =>
      val (stmts, _) = AsQuadruples(mi.method,p.classHierarchy);
      seeds ++ stmts.filter(s => Util.containsInvokeExpr(s) && Util.isSink(s)).map(s => MStmt(s, s.pc, mi.method))
    }
    println(s"${seeds.size} forName seeds")
    seeds
  }
}

object TaintAnalysis extends App {
  new TaintAnalysis().executeAnalysis(args(0).toInt, args(1))
}
