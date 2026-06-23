/*
 *    MoaStyleDynseStaggerTestbed.java
 *    Testbed for running the MOA-style Dynse port in the original project.
 */
package br.ufpr.dynse.testbed;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.ufpr.dynse.core.MoaStyleDynse;
import br.ufpr.dynse.core.UFPRLearningCurve;
import br.ufpr.dynse.evaluation.EvaluatePeriodicHeldOutTestUFPR;
import br.ufpr.dynse.generator.StaggerDriftGenerator;
import br.ufpr.dynse.util.UFPRLearningCurveUtils;
import moa.tasks.StandardTaskMonitor;
import moa.tasks.TaskMonitor;

public class MoaStyleDynseStaggerTestbed implements MultipleExecutionsTestbed {

	private Random random = new Random(1);
	private UFPRLearningCurveUtils ufprLearningCurveUtils = new UFPRLearningCurveUtils();

	@Override
	public void executeTests(int numExec) throws Exception {
		List<UFPRLearningCurve> learningCurves = new ArrayList<UFPRLearningCurve>(numExec);
		List<Integer> poolSizes = new ArrayList<Integer>();
		for (int i = 0; i < numExec; i++) {
			System.out.println("Executing MOA-style Dynse KE - Exec.: " + i);
			TaskMonitor monitor = new StandardTaskMonitor();
			EvaluatePeriodicHeldOutTestUFPR evaluator = new EvaluatePeriodicHeldOutTestUFPR();

			MoaStyleDynse dynse = new MoaStyleDynse(
					5,
					4,
					25,
					StaggerDriftGenerator.NUM_INST_TRAIN_CLASSIFIER_STAGGER);
			evaluator.learnerOption.setCurrentObject(dynse);
			System.out.println(random.nextInt());
			evaluator.streamOption.setCurrentObject(new StaggerDriftGenerator(random.nextInt()));
			evaluator.trainSizeOption.setValue((StaggerDriftGenerator.NUM_INST_TRAIN_CLASSIFIER_STAGGER
					+ StaggerDriftGenerator.NUM_INST_TEST_CLASSIFIER_STAGGER) * 10 * 4);
			evaluator.testSizeOption.setValue(StaggerDriftGenerator.NUM_INST_TEST_CLASSIFIER_STAGGER);
			evaluator.sampleFrequencyOption.setValue(StaggerDriftGenerator.NUM_INST_TRAIN_CLASSIFIER_STAGGER);
			evaluator.prepareForUse();

			UFPRLearningCurve learningCurve = (UFPRLearningCurve) evaluator.doTask(monitor, null);
			learningCurves.add(learningCurve);
			poolSizes.add(dynse.getPoolSize());
		}

		UFPRLearningCurve avgResult = ufprLearningCurveUtils.averageResults(learningCurves);
		System.out.println(ufprLearningCurveUtils.strMainStatisticsMatlab(avgResult));
	}

}
