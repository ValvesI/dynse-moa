/*
 *    MoaStyleDynseForestCoverTypeTestBed.java
 *    Testbed for running the MOA-style Dynse port on the Forest CoverType dataset.
 */
package br.ufpr.dynse.testbed;

import java.util.ArrayList;
import java.util.List;

import br.ufpr.dynse.constant.Constants;
import br.ufpr.dynse.core.MoaStyleDynse;
import br.ufpr.dynse.core.UFPRLearningCurve;
import br.ufpr.dynse.evaluation.EvaluatePeriodicHeldOutTestUFPR;
import br.ufpr.dynse.util.UFPRLearningCurveUtils;
import moa.streams.ArffFileStream;
import moa.tasks.StandardTaskMonitor;
import moa.tasks.TaskMonitor;

public class MoaStyleDynseForestCoverTypeTestBed implements MultipleExecutionsTestbed {

	private static final String PATH_DATASET = DatasetPaths.FOREST_COVERTYPE;

	private UFPRLearningCurveUtils ufprLearningCurveUtils = new UFPRLearningCurveUtils();

	@Override
	public void executeTests(int numExec) throws Exception {
		List<UFPRLearningCurve> learningCurves = new ArrayList<UFPRLearningCurve>(numExec);

		for (int i = 0; i < numExec; i++) {
			TaskMonitor monitor = new StandardTaskMonitor();
			EvaluatePeriodicHeldOutTestUFPR evaluator = new EvaluatePeriodicHeldOutTestUFPR();

			ArffFileStream stream = new ArffFileStream();
			stream.arffFileOption.setValue(PATH_DATASET);

			MoaStyleDynse dynse = new MoaStyleDynse(
					5,
					4,
					25,
					Constants.NUM_INST_TRAIN_CLASSIFIER_FOREST);
			evaluator.learnerOption.setCurrentObject(dynse);

			System.out.println("Running " + i + " MoaStyleDynseForestKE");

			evaluator.streamOption.setCurrentObject(stream);
			evaluator.testSizeOption.setValue(Constants.NUM_INST_TEST_CLASSIFIER_FOREST);
			evaluator.sampleFrequencyOption.setValue(Constants.NUM_INST_TRAIN_CLASSIFIER_FOREST);
			evaluator.prepareForUse();

			UFPRLearningCurve learningCurve = (UFPRLearningCurve) evaluator.doTask(monitor, null);
			System.out.println(ufprLearningCurveUtils.strMainStatisticsMatlab(learningCurve));
			learningCurves.add(learningCurve);
		}

		UFPRLearningCurve avgResult = ufprLearningCurveUtils.averageResults(learningCurves);
		System.out.println(ufprLearningCurveUtils.strMainStatisticsMatlab(avgResult));
	}
}
