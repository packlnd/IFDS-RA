package taintanalysis

import org.opalj.br.analyses.Project

sealed trait Type
case object InnerToOuter extends Type

case class Context(icfg: TACOpalICFG, p: Project[_], tpe: Type)
