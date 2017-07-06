package flow.twist;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import soot.Body;
import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.Transform;
import soot.jimple.Jimple;
import soot.jimple.NopStmt;

import soot.G;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.nio.file.Files;
import java.io.File;

import util.Util;

public abstract class AbstractAnalysis {

  public void execute() {
    execute("#", "#", "#", -1);
  }

	public void execute(String ifds, String sa_lib, String ta, int numThreads) {
    for (int j=0; j<10; ++j) {
      ArrayList<String> argList = createArgs();
      AnalysisReporting.setSootArgs(argList);
      registerAnalysisTransformer(numThreads);
      soot.Main.main(argList.toArray(new String[0]));
      try {
        Files.copy(new File("./results/stats.txt").toPath(), new File("./" + ifds + "_" + sa_lib + "_" + ta + "_" + numThreads + "_" + j).toPath(), REPLACE_EXISTING);
      } catch (Exception e) {}
      G.reset();
      Util.runGC();
    }
	}

	protected abstract ArrayList<String> createArgs();

	protected abstract void executeAnalysis();

	private static void insertNopStatements() {
		for (Iterator<MethodOrMethodContext> iter = Scene.v().getReachableMethods().listener(); iter.hasNext();) {
			SootMethod m = iter.next().method();
			if (m.hasActiveBody()) {
				Body b = m.getActiveBody();
				NopStmt newNopStmt = Jimple.v().newNopStmt();
				newNopStmt.addAllTagsOf(b.getUnits().getFirst());
				b.getUnits().addFirst(newNopStmt);

				ActiveBodyVerifier.markActive(m);
			} else
				ActiveBodyVerifier.markInactive(m);
		}
	}

	private void registerAnalysisTransformer(int threads) {
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.permissionAnalysis", new SceneTransformer() {
			protected void internalTransform(String phaseName, @SuppressWarnings("rawtypes") Map options) {
				Stats.print();
				insertNopStatements();
				executeAnalysis();
			}
		}));
	}
}
