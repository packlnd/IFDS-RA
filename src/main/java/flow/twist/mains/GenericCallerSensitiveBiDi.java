package flow.twist.mains;

import java.util.Set;

import flow.twist.AbstractMainAnalysis;
import flow.twist.SolverFactory;
import flow.twist.config.AnalysisConfigurationBuilder;
import flow.twist.path.Path;
import flow.twist.pathchecker.FilterSingleDirectionReports;
import flow.twist.reporter.ConsoleReporter;
import flow.twist.transformer.StoreDataTransformer;
import flow.twist.transformer.path.MergeEqualSelectorStrategy;
import flow.twist.transformer.path.PathBuilderResultTransformer;

public class GenericCallerSensitiveBiDi {

	public static void main(final String[] a) {
    int threads = Integer.parseInt(a[0]);
		new AbstractMainAnalysis(java.util.Arrays.copyOfRange(a, 1, a.length)) {
			@Override
			protected void executeAnalysis() {
				SolverFactory.runBiDirectionSolver(AnalysisConfigurationBuilder.i2oGenericCallerSensitiveDefaults().reporter(
              new ConsoleReporter()), threads);
			}
			@Override
			protected Set<Path> _executeAnalysis() {
        return null;
			}
		}.execute("heros", "soot", "gen", threads);
	}
}
