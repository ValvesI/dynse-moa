/*
 *    MoaStyleDynseSeaTestbed.java
 *    Testbed for running the MOA-style Dynse port on the SEA concepts stream.
 */
package br.ufpr.dynse.testbed;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.ufpr.dynse.constant.Constants;
import br.ufpr.dynse.core.MoaStyleDynse;
import br.ufpr.dynse.core.UFPRLearningCurve;
import br.ufpr.dynse.evaluation.EvaluatePeriodicHeldOutTestUFPR;
import br.ufpr.dynse.generator.SeaDriftGenerator;
import br.ufpr.dynse.util.UFPRLearningCurveUtils;
import moa.tasks.StandardTaskMonitor;
import moa.tasks.TaskMonitor;

public class MoaStyleDynseSeaTestbed implements MultipleExecutionsTestbed {

	private Random random = new Random(1);
	private UFPRLearningCurveUtils ufprLearningCurveUtils = new UFPRLearningCurveUtils();

	@Override
	public void executeTests(int numExec) throws Exception {
		List<UFPRLearningCurve> learningCurves = new ArrayList<UFPRLearningCurve>(numExec);

		for (int i = 0; i < numExec; i++) {
			System.out.println("Executing MOA-style Dynse SEA - Exec.: " + i);
			TaskMonitor monitor = new StandardTaskMonitor();
			EvaluatePeriodicHeldOutTestUFPR evaluator = new EvaluatePeriodicHeldOutTestUFPR();

			MoaStyleDynse dynse = new MoaStyleDynse(5, 4, 25, Constants.NUM_INST_TRAIN_TEST_SEA);
			evaluator.learnerOption.setCurrentObject(dynse);

			evaluator.streamOption.setCurrentObject(new SeaDriftGenerator(random.nextInt()));
			evaluator.trainSizeOption.setValue(50000);
			evaluator.testSizeOption.setValue(Constants.NUM_INST_TRAIN_TEST_SEA);
			evaluator.sampleFrequencyOption.setValue(Constants.NUM_INST_TRAIN_TEST_SEA);
			evaluator.prepareForUse();

			UFPRLearningCurve learningCurve = (UFPRLearningCurve) evaluator.doTask(monitor, null);
			learningCurves.add(learningCurve);
		}

		UFPRLearningCurve avgResult = ufprLearningCurveUtils.averageResults(learningCurves);
		System.out.println(ufprLearningCurveUtils.strMainStatisticsMatlab(avgResult));
	}
}
