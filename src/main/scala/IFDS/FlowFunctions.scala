/**
  * Created by star on 02/03/17.
  */
package IFDS

trait FlowFunctions[N,D,M] {
  //passArgs(n#)
  def getNormalFlowFunction(curr: N, succ: N): FlowFunction[D]

  //callFlow(n#)
  def getCallFlowFunction(callStmt: N, destinationMethod: M): FlowFunction[D]

  //returnVal(n#)
  def getReturnFlowFunction(callSite: Option[N], calleeMethod: M, exitStmt: N, returnSite: Option[N]): FlowFunction[D]

  //callFlow(n#)
  def getCallToReturnFlowFunction(callSite: N, returnSite: N): FlowFunction[D]
}

