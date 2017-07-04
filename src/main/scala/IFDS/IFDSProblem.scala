/**
  * Created by star on 02/03/17.
  */

package IFDS

/**
  * Based on the definition 2.4 in paper 95' Reps, Horwitz & Sagiv
  * IFDS problem is a five-tuple (G*, D, F, M, join)
  * @tparam N
  * @tparam D finite set
  * @tparam M
  */
trait IFDSProblem[N, D, M] {
  def getIcfg: HerosICFG[N, M]
  def getFlowFunctions: FlowFunctions[N, D, M]
  def getZeroValue: D
  def getInitialSeeds: Map[N, Set[D]]
  def getFollowReturnsPastSeeds: Boolean
}
