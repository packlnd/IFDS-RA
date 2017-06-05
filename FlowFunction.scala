/**
  * Created by star on 02/03/17.
  */
package IFDS

trait FlowFunction[D] {
  /**
    * @param d a fact
    * @return the target fact
    */
  def apply(d: D): Set[D] = {
    computeTargets(d)
  }

  def computeTargets(d: D): Set[D] = ???
    /*val funcs: FlowFunction[D]
    val curr: HashSet[D]
    curr.+(d)
    for (func <- funcs) {
      val next: HashSet[D]
      for(d <- curr)
        next.+(func.computeTargets(d))
      curr = next
    }
    return curr
  */
}
