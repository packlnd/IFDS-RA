package taintanalysis

import org.opalj.br.analyses.Project
import java.io.File

import IFDS.HerosICFG

import org.opalj.ai.analyses.cg.ComputedCallGraph
import org.opalj.ai.analyses.cg.VTACallGraphKey
import heros.InterproceduralCFG
import org.opalj.br.instructions.Instruction
import org.opalj.br.Method
import java.util.{ List ⇒ JList }
import java.util.{ Collection ⇒ JCollection }
import java.util.{ Set ⇒ JSet }
import org.opalj.ai.analyses.cg.CallGraph
import java.util.Collections
import org.opalj.br.instructions.INVOKESPECIAL
import scala.collection.JavaConverters._
import org.opalj.br.cfg.CFG
import org.opalj.br.cfg.CFGFactory
import java.util.concurrent.ConcurrentHashMap
import org.opalj.br.cfg.CatchNode
import org.opalj.br.cfg.BasicBlock
import org.opalj.br.instructions.MethodInvocationInstruction
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.HashSet
import heros.solver.IDESolver
import heros.IFDSTabulationProblem
import org.opalj.br.instructions.INVOKEVIRTUAL
import org.opalj.br.instructions.INVOKESTATIC
import org.opalj.ai.domain.RecordCFG
import org.opalj.ai.Domain
import org.opalj.ai.AIResult
import org.opalj.ai.domain.l1.DefaultDomainWithCFG
import org.opalj.ai.BaseAI
import org.opalj.ai.domain.RecordDefUse
import org.opalj.ai.domain.l1.DefaultDomainWithCFGAndDefUse

object Direction extends Enumeration {
  val FORWARDS, BACKWARDS = Value
}

case class MInstruction(i: Instruction, pc: Int, m: Method)

class OpalICFG(cg: CallGraph) extends HerosICFG[MInstruction, Method] { //InterproceduralCFG[MInstruction, Method] {

    //    val cfgs: ConcurrentHashMap[Method, CFG] = {
    //        val cfgs = new ConcurrentHashMap[Method, CFG]
    //        cg.project.parForeachMethodWithBody() { methodInfo ⇒
    //            val m = methodInfo.method
    //            cfgs.put(m, CFGFactory(m.body.get, cg.project.classHierarchy))
    //        }
    //        cfgs
    //    }

    val aiCFGs: ConcurrentHashMap[Method, AIResult { val domain: Domain with RecordCFG with RecordDefUse }] = {
        val aiCFGs = new ConcurrentHashMap[Method, AIResult { val domain: Domain with RecordCFG with RecordDefUse }]
        cg.project.parForeachMethodWithBody() { methodInfo ⇒
            val c = methodInfo.classFile
            val m = methodInfo.method
            val aiResult = BaseAI(c, m, new DefaultDomainWithCFGAndDefUse(cg.project, c, m))
            aiCFGs.put(m, aiResult)
        }
        aiCFGs
    }

    def getMethodOf(n: MInstruction): Method = {
        n.m
    }

    //    def getPredsOf(instr: MInstruction): JList[MInstruction] = {
    //        if (instr.pc == 0)
    //            return Collections.emptyList()
    //
    //        val bb = cfgs.get(instr.m).bb(instr.pc)
    //        if (bb.startPC == instr.pc) {
    //            val prevInstrPCs = bb.predecessors.flatMap {
    //                case cn: CatchNode  ⇒ cn.predecessors.map(_.asBasicBlock)
    //                case bb: BasicBlock ⇒ Seq(bb)
    //            }.map(_.endPC)
    //            prevInstrPCs.map { prevInstrPC ⇒
    //                MInstruction(instr.m.body.get.instructions(prevInstrPC), prevInstrPC, instr.m)
    //            }.toList.asJava
    //        } else {
    //            val prevInstrPC = instr.m.body.get.pcOfPreviousInstruction(instr.pc)
    //            Collections.singletonList(MInstruction(instr.m.body.get.instructions(prevInstrPC), prevInstrPC, instr.m))
    //        }
    //    }

    def getPredsOf(instr: MInstruction): JList[MInstruction] = {
        if (instr.pc == 0)
            return Collections.emptyList()

        aiCFGs.get(instr.m).domain.predecessorsOf(instr.pc).mapToList { predPC ⇒
            MInstruction(instr.m.body.get.instructions(predPC), predPC, instr.m)
        }.asJava
    }

    //    def getSuccsOf(instr: MInstruction): JList[MInstruction] = {
    //        val pcs = cfgs.get(instr.m).successors(instr.pc)
    //        pcs.map { succPc ⇒
    //            MInstruction(instr.m.body.get.instructions(succPc), succPc, instr.m)
    //        }.toList.asJava
    //    }

    def getSuccessorsOf(instr: MInstruction): Set[MInstruction] = {
        val pcs = aiCFGs.get(instr.m).domain.successorsOf(instr.pc, regularSuccessorOnly = false)
        pcs.map { succPc ⇒
            MInstruction(instr.m.body.get.instructions(succPc), succPc, instr.m)
        }.toSet
    }

    def getCalleesOfCallAt(callInstr: MInstruction): Set[Method] = {
        cg.calls(callInstr.m, callInstr.pc).toSet
    }

    def getCallersOf(m: Method): Set[MInstruction] = {
        val res = cg.calledBy(m).flatMap { e ⇒
            val (m, pcs) = e
            val instr = m.body.get.instructions
            pcs.map { pc ⇒ MInstruction(instr(pc), pc, m) }
        }
        res.toSet
    }

    def getCallsFromWithin(m: Method): JSet[MInstruction] = {
        val res = m.body.get.collectWithIndex {
            case (pc, i: INVOKESPECIAL) ⇒ MInstruction(i, pc, m)
        }
        res.toSet.asJava
    }

    def getStartPointsOf(m: Method): Set[MInstruction] = {
        Set(MInstruction(m.body.get.instructions(0), 0, m))
    }

    def getReturnSitesOfCallAt(callInstr: MInstruction): Set[MInstruction] = {
        getSuccessorsOf(callInstr)
    }

    def isCallStatement(stmt: MInstruction): Boolean = {
        stmt.i.isInstanceOf[MethodInvocationInstruction]
    }

    def isExitStatement(stmt: MInstruction): Boolean = {
        getSuccessorsOf(stmt).isEmpty
    }

    def isStartPoint(stmt: MInstruction): Boolean = {
        stmt.pc == 0
    }

    def allNonCallStartNodes(): JSet[MInstruction] = {
        val res = new ConcurrentLinkedQueue[MInstruction]
        cg.project.parForeachMethodWithBody() { mi ⇒
            val m = mi.method
            m.body.get.iterate { (pc, instr) ⇒
                if (pc != 0 && !(instr.isInstanceOf[MethodInvocationInstruction]))
                    res.add(MInstruction(instr, pc, m))
            }
        }
        new HashSet(res)
    }

    def isFallThroughSuccessor(stmt: MInstruction, succ: MInstruction): Boolean = ???

    def isBranchTarget(stmt: MInstruction, succ: MInstruction): Boolean = ???

}
