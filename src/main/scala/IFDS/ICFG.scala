/**
  * Created by star on 02/03/17.
  */
package IFDS

/**
  * Based on the Definition 2.1 of paper 95' Reps, Horwitz, Sagiv
  * @tparam N Nodes in the supergraph
  * @tparam M Method for the supergraph
  */
abstract class ICFG[N, M] private {

  /**************************************************************************************
    * Some basic properties of the superGraph
    *************************************************************************************/
  /**
    * @param n a node in the supergraph
    * @return the predecessor nodes
    */
  def getPredecessorsOf(n: N): Set[N] = ???

  /**
    * @param n a node in the supergraph
    * @return the successor nodes
    */
  def getSuccessorsOf(n: N): Set[N] = ???

  /**
    * @param m a method/procedure
    * @return the start nodes for procedure
    */
  def getStartPointsOf(m: M): Set[N] = ???

  /**
    * @param m a method/procedure
    * @return the end nodes for procedure
    */
  def getExitPointsOf(m: M): Set[N] = ???

  /**************************************************************************************
    * Special nodes in superGraph
    *************************************************************************************/
  /**
    * @param n a node in the supergraph
    * @return true iff the node is a start node s_p for a procedure
    */
  def isStart(n: N): Boolean = ???

  /**
    * @param n a node in the supergraph
    * @return true iff the node is an exit node
    */
  def isExit(n: N): Boolean = ???

  /**
    * @param n a node in the supergraph
    * @return true iff the node includes a call
    */
  def isCall(n: N): Boolean = ???

  /**
    * @param n a node in the supergraph
    * @return true iff the node is a return site
    */
  def isReturnSite(n: N, m: M): Boolean = ???
  /**************************************************************************************
    * Special edges in supergraph
    *************************************************************************************/

  def isOrdinaryEdge(n: N, nSucsOfN: N, m: M): Boolean = ???

  def isCallToRetureSiteEdge(n: N, nSucsOfN: N, m: M): Boolean = ???

  def isCallToStartEdge(n: N, nSucsOfN: N, m: M): Boolean = ???

  def isExitToReturnEdge(n: N, nSucsOfN: N, m: M): Boolean = ???

  /**************************************************************************************
    * Four functions for Tabulation Algorithm in paper 95' Reps, Horsitz & Sagiv
    *************************************************************************************/
  /**
    * @param n a call node in the supergraph
    * @return all statements to which a call could return (same as getReturnSitesOfCallAt)
    */
  def getReturnSites(n: N): Set[N] = ???

  /**
    * @param n a node in the supergraph
    * @return a method/procedure containing a node (same as getMethodOf)
    */
  def getProcOf(n: N): M = ???

  /**
    * @param n a call node in the supergraph
    * @return all callee methods/procedure for a given call (same as getCalleesOfCallAt)
    */
  def getCalledProcsOf(n: N): Set[M] = ???

  /**
    * @param n a method/procedure
    * @return all caller statements/nodes of a given method
    */
  def getCallersOf(m: M): Set[N] = ???

  // Probably not needed, at least not now
  // /**************************************************************************************
  //   * Four functions in extension-IFDS: 10' Naeem et al.
  //   *************************************************************************************/
  // /**
  //   * @param n a normal node
  //   * @return all intraprocedural edges
  //   */
  // def flow(n: N): E = ???

  // /**
  //   * @param n a call node
  //   * @return all call-to-start edges when n is at a call site
  //   */
  // def passArgs(n: N): E = ???

  // /**
  //   * @param n at the exit of a procedure
  //   * @return all exit-to-return-site edges when n is at a call site
  //   */
  // def returnVal(n: N): E = ???

  // /**
  //   * @param n a call node
  //   * @return all call-to-return-site edges when n is at a call site
  //   */
  // def callFlow(n: N): E = ???

  /**************************************************************************************
   * Same as Heros ICFG interface to make code easier to read
   *************************************************************************************/
  /**
   * Returns all callee methods for a given call.
   */
  def getCalleesOfCallAt(n: N): Set[M] = ???

  /**
   *
   */

  /**************************************************************************************
    * Other stuff discussed in the paper
    *************************************************************************************/
  def numberOfCallNodes() = ???

}

object ICFG {
  //def empty[N]: ICFG[N, E, P] = new ICFG(Set.empty, Set.empty, Set.empty)
}

