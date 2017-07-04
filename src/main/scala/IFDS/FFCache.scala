package IFDS

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

class FFCache[N, D, M](delegate: FlowFunctions[N, D, M], builder: CacheBuilder[Object, Object]) extends FlowFunctions[N, D, M] {
  case class NormalKey(curr: N, succ: N)
  case class CallKey(callSite: N, destinationMethod: M)
  case class ReturnKey(callSite: Option[N], calleeMethod: M, exitStmt: N, returnSite: Option[N])
  case class CallToReturnKey(callSite: N, returnSite: N)

  val normalCache = builder.build(new CacheLoader[NormalKey, FlowFunction[D]] {
    def load(key: NormalKey): FlowFunction[D] = {
      delegate.getNormalFlowFunction(key.curr, key.succ)
    }
  })
  val callCache = builder.build(new CacheLoader[CallKey, FlowFunction[D]] {
    def load(key: CallKey): FlowFunction[D] = {
      delegate.getCallFlowFunction(key.callSite, key.destinationMethod)
    }
  })
  val returnCache = builder.build(new CacheLoader[ReturnKey, FlowFunction[D]] {
    def load(key: ReturnKey): FlowFunction[D] = {
      delegate.getReturnFlowFunction(key.callSite, key.calleeMethod, key.exitStmt, key.returnSite)
    }
  })
  val callToReturnCache = builder.build(new CacheLoader[CallToReturnKey, FlowFunction[D]] {
    def load(key: CallToReturnKey): FlowFunction[D] = {
      delegate.getCallToReturnFlowFunction(key.callSite, key.returnSite)
    }
  })

  def getNormalFlowFunction(curr: N, succ: N): FlowFunction[D] = {
    normalCache.getUnchecked(NormalKey(curr, succ))
  }

  def getCallFlowFunction(callStmt: N, destinationMethod: M): FlowFunction[D] = {
    callCache.getUnchecked(CallKey(callStmt, destinationMethod))
  }

  def getReturnFlowFunction(callSite: Option[N], calleeMethod: M, exitStmt: N, returnSite: Option[N]): FlowFunction[D] = {
    returnCache.getUnchecked(ReturnKey(callSite, calleeMethod, exitStmt, returnSite))
  }

  def getCallToReturnFlowFunction(callSite: N, returnSite: N): FlowFunction[D] = {
    callToReturnCache.getUnchecked(CallToReturnKey(callSite, returnSite))
  }
}
