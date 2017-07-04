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

public class ClassForNameBiDi {

	public static void main(final String[] args) {
		new AbstractMainAnalysis(args) {
      @Override
			protected void executeAnalysis(int threads) {
				SolverFactory.runBiDirectionSolver(AnalysisConfigurationBuilder.i2oSimpleClassForNameDefaults().reporter(
						new ConsoleReporter()), threads);
      }
			@Override
			protected Set<Path> _executeAnalysis() {
        return null;
			}
		}.execute();
	}
}
