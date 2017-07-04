package flow.twist;

import static flow.twist.config.AnalysisDirection.BACKWARDS;
import static flow.twist.config.AnalysisDirection.FORWARDS;
import heros.solver.BiDiIFDSSolver;
import heros.solver.IFDSSolver;
import heros.solver.JoinHandlingNodesIFDSSolver;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
import flow.twist.config.AnalysisConfigurationBuilder;
import flow.twist.config.AnalysisConfigurationBuilder.PropagationReporterDecorator;
import flow.twist.ifds.FlowFunctionFactory;
import flow.twist.ifds.TabulationProblem;
import flow.twist.reporter.DelayingReporter;
import flow.twist.reporter.IfdsReporter;
import flow.twist.trackable.Trackable;
import flow.twist.util.Pair;

import taintanalysis.RunRASootSolver;
import taintanalysis.RunBiDiRASootSolver;
import taintanalysis.RunRASEQSootSolver;

public class SolverFactory {

	public static void runRASEQSolver(AnalysisConfigurationBuilder configBuilder, int threads) {

		Pair<IfdsReporter, AnalysisConfigurationBuilder> pair = configBuilder.decorateReporter(new PropagationReporterDecorator() {
			@Override
			public IfdsReporter decorate(IfdsReporter reporter) {
				return new DelayingReporter(reporter);
			}
		});

    RunRASEQSootSolver rss = new RunRASEQSootSolver(pair.second.build(), threads);
		AnalysisReporting.analysisStarted();

		AnalysisReporting.ifdsStarting(rss.tpContext(), rss);
    rss.run();
		AnalysisReporting.ifdsFinished(rss.tpContext(), rss, 0);
		pair.first.analysisFinished();
		AnalysisReporting.analysisFinished();
	}

	public static void runRASolver(AnalysisConfigurationBuilder configBuilder, int threads) {

		Pair<IfdsReporter, AnalysisConfigurationBuilder> pair = configBuilder.decorateReporter(new PropagationReporterDecorator() {
			@Override
			public IfdsReporter decorate(IfdsReporter reporter) {
				return new DelayingReporter(reporter);
			}
		});

    RunRASootSolver rss = new RunRASootSolver(pair.second.build(), threads);
		AnalysisReporting.analysisStarted();

		AnalysisReporting.ifdsStarting(rss.tpContext(), rss);
    rss.run();
		AnalysisReporting.ifdsFinished(rss.tpContext(), rss, 0);
		pair.first.analysisFinished();
		AnalysisReporting.analysisFinished();
	}

	public static void runOneDirectionSolver(AnalysisConfigurationBuilder configBuilder, int numThreads) {

		Pair<IfdsReporter, AnalysisConfigurationBuilder> pair = configBuilder.decorateReporter(new PropagationReporterDecorator() {
			@Override
			public IfdsReporter decorate(IfdsReporter reporter) {
				return new DelayingReporter(reporter);
			}
		});
		
		TabulationProblem problem = createTabulationProblem(pair.second, numThreads);
		IFDSSolver<Unit, Trackable, SootMethod, BiDiInterproceduralCFG<Unit, SootMethod>> solver = createSolver(problem);

		AnalysisReporting.analysisStarted();
		
		_runOneDirectionSolver(problem, solver);
		pair.first.analysisFinished();
		AnalysisReporting.analysisFinished();
	}

	protected static void _runOneDirectionSolver(final TabulationProblem tabulationProblem, IFDSSolver<Unit, Trackable, SootMethod, BiDiInterproceduralCFG<Unit, SootMethod>> solver) {
		AnalysisReporting.ifdsStarting(tabulationProblem.getContext(), solver);
		solver.solve();
		AnalysisReporting.ifdsFinished(tabulationProblem.getContext(), solver, FlowFunctionFactory.propCounter.get());
		FlowFunctionFactory.propCounter.set(0);
	}

	private static IFDSSolver<Unit, Trackable, SootMethod, BiDiInterproceduralCFG<Unit, SootMethod>> createSolver(
			final TabulationProblem tabulationProblem) {
		IFDSSolver<Unit, Trackable, SootMethod, BiDiInterproceduralCFG<Unit, SootMethod>> solver = new JoinHandlingNodesIFDSSolver<Unit, Trackable, SootMethod, BiDiInterproceduralCFG<Unit, SootMethod>>(
				tabulationProblem) {
			@Override
			protected String getDebugName() {
				if (tabulationProblem.getContext().direction == FORWARDS)
					return "FW";
				else
					return "BW";
			}
		};
		return solver;
	}

	private static TabulationProblem createTabulationProblem(
			AnalysisConfigurationBuilder configBuilder,
      int numThreads
  ) {
		final TabulationProblem tabulationProblem = new TabulationProblem(configBuilder.build(), numThreads);
		return tabulationProblem;
	}

	public static void runInnerToOuterSolver(AnalysisConfigurationBuilder configBuilder) {}

	public static void runBiDiRASolver(AnalysisConfigurationBuilder configBuilder, int threads) {
		Pair<IfdsReporter, AnalysisConfigurationBuilder> pair = configBuilder.decorateReporter(new PropagationReporterDecorator() {
			@Override
			public IfdsReporter decorate(IfdsReporter reporter) {
				return new DelayingReporter(reporter);
			}
		});
		configBuilder = pair.second;

    RunBiDiRASootSolver rss = new RunBiDiRASootSolver(configBuilder.direction(FORWARDS).build(), configBuilder.direction(BACKWARDS).build(), threads);
		AnalysisReporting.analysisStarted();
		
		AnalysisReporting.ifdsStarting(rss.backwardsContext(), rss);
		AnalysisReporting.ifdsStarting(rss.forwardsContext(), rss);
    rss.run();
		AnalysisReporting.ifdsFinished(rss.backwardsContext(), rss, 0);
		AnalysisReporting.ifdsFinished(rss.forwardsContext(), rss, 0);
		pair.first.analysisFinished();
		AnalysisReporting.analysisFinished();
	}

	public static void runBiDirectionSolver(AnalysisConfigurationBuilder configBuilder, int threads) {
		Pair<IfdsReporter, AnalysisConfigurationBuilder> pair = configBuilder.decorateReporter(new PropagationReporterDecorator() {
			@Override
			public IfdsReporter decorate(IfdsReporter reporter) {
				return new DelayingReporter(reporter);
			}
		});
		configBuilder = pair.second;

		TabulationProblem backwardsTabulationProblem = new TabulationProblem(configBuilder.direction(BACKWARDS).build(), threads);
		TabulationProblem forwardsTabulationProblem = new TabulationProblem(configBuilder.direction(FORWARDS).build(), threads);

		BiDiIFDSSolver<Unit, Trackable, SootMethod, BiDiInterproceduralCFG<Unit, SootMethod>> biDiIFDSSolver = new BiDiIFDSSolver<Unit, Trackable, SootMethod, BiDiInterproceduralCFG<Unit, SootMethod>>(
				forwardsTabulationProblem, backwardsTabulationProblem);

		AnalysisReporting.analysisStarted();
		
		AnalysisReporting.ifdsStarting(backwardsTabulationProblem.getContext(), biDiIFDSSolver);
		AnalysisReporting.ifdsStarting(forwardsTabulationProblem.getContext(), biDiIFDSSolver);
		biDiIFDSSolver.solve();
		AnalysisReporting.ifdsFinished(backwardsTabulationProblem.getContext(), biDiIFDSSolver, FlowFunctionFactory.propCounter.get());
		AnalysisReporting.ifdsFinished(forwardsTabulationProblem.getContext(), biDiIFDSSolver, FlowFunctionFactory.propCounter.get());
		pair.first.analysisFinished();
		AnalysisReporting.analysisFinished();
	}
}
