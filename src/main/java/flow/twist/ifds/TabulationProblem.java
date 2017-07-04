package flow.twist.ifds;

import heros.DefaultSeeds;
import heros.FlowFunctions;

import java.util.Map;
import java.util.Set;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.ide.DefaultJimpleIFDSTabulationProblem;
import soot.jimple.toolkits.ide.icfg.BackwardsInterproceduralCFG;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import flow.twist.config.AnalysisConfiguration;
import flow.twist.config.AnalysisContext;
import flow.twist.config.AnalysisDirection;
import flow.twist.targets.AnalysisTarget;
import flow.twist.trackable.Trackable;
import flow.twist.trackable.Zero;

public class TabulationProblem extends DefaultJimpleIFDSTabulationProblem<Trackable, BiDiInterproceduralCFG<Unit, SootMethod>> {

	private AnalysisContext context;
	private FlowFunctionFactory flowFunctionFactory;
  private int numThreads;

	public TabulationProblem(AnalysisConfiguration config, int numThreads) {
		super(makeICFG(config.direction));
		this.context = new AnalysisContext(config, interproceduralCFG());
    this.numThreads = numThreads;
		flowFunctionFactory = new FlowFunctionFactory(this.context);
	}

  @Override
  public int numThreads() {
    System.out.println("Number of threads: " + this.numThreads);
    return this.numThreads;
  }

	private static BiDiInterproceduralCFG<Unit, SootMethod> makeICFG(AnalysisDirection direction) {
		JimpleBasedInterproceduralCFG fwdIcfg = new JimpleBasedInterproceduralCFG();
		if (direction == AnalysisDirection.FORWARDS)
			return fwdIcfg;
		else
			return new BackwardsInterproceduralCFG(fwdIcfg);
	}

	@Override
	public Map<Unit, Set<Trackable>> initialSeeds() {
    Map<Unit, Set<Trackable>> is = DefaultSeeds.make(context.seedFactory.initialSeeds(context), zeroValue());
    return is;
	}

	public void startReturningDeferredTargets() {
		for (AnalysisTarget target : context.targets) {
			target.enableIfDeferred();
		}
	}

	@Override
	protected FlowFunctions<Unit, Trackable, SootMethod> createFlowFunctionsFactory() {
		return flowFunctionFactory;
	}

	@Override
	protected Trackable createZeroValue() {
		return Zero.ZERO;
	}

	@Override
	public boolean autoAddZero() {
		return false;
	}

	@Override
	public boolean followReturnsPastSeeds() {
		// unbalanced analysis problem
		return true;
	}

	@Override
	public boolean computeValues() {
		return false;
	}

	public AnalysisContext getContext() {
		return context;
	}
}
