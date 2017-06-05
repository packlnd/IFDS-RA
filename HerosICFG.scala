package IFDS

trait HerosICFG[N, M] {

  /**
   * Returns true if n represents a call statement
   */
  def isCallStatement(n: N): Boolean

  /**
   * Returns true if n represents an exit statement
   */
  def isExitStatement(n: N): Boolean

  /**
   * From Heros:
   * Returns all statements to which a call could return.
   * In the RHS paper, for every call there is just one return site.
   * We, however, use as return site the successor statements, of which
   * there can be many in case of exceptional flow.
   */
  def getReturnSitesOfCallAt(n: N): Set[N]

  def getSuccessorsOf(n: N): Set[N]

  /**
   * Returns all callee methods for a given call.
   */
  def getCalleesOfCallAt(n: N): Set[M]

  /**
   * Returns all start points of a given method. There may be
   * more than one start point in case of a backward analysis.
   */
  def getStartPointsOf(m: M): Set[N]

  def getMethodOf(n: N): M

  def getCallersOf(m: M): Set[N]
}
