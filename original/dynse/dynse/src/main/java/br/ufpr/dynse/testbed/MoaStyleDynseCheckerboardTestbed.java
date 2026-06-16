/*
 *    MoaStyleDynseCheckerboardTestbed.java
 *    Testbed for running the MOA-style Dynse port on the Checkerboard dataset.
 */
package br.ufpr.dynse.testbed;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import br.ufpr.dynse.constant.Constants;
import br.ufpr.dynse.core.MoaStyleDynse;
import br.ufpr.dynse.core.UFPRLearningCurve;
import br.ufpr.dynse.dataset.converter.CheckerboardElwellPolikarConverter;
import br.ufpr.dynse.evaluation.CheckerboardTestUFPR;
import br.ufpr.dynse.util.UFPRLearningCurveUtils;
import moa.streams.ArffFileStream;
import moa.tasks.StandardTaskMonitor;
import moa.tasks.TaskMonitor;

public class MoaStyleDynseCheckerboardTestbed implements MultipleExecutionsTestbed {

	private UFPRLearningCurveUtils ufprLearningCurveUtils = new UFPRLearningCurveUtils();

	@Override
	public void executeTests(int numExecutions) throws Exception {
		ensureCheckerboardArffFiles();
		List<UFPRLearningCurve> learningCurves = new ArrayList<UFPRLearningCurve>(numExecutions);

		for (int i = 0; i < numExecutions; i++) {
			System.out.println("Running MOA-style Dynse Checkerboard KE - Exec.: " + i);
			TaskMonitor monitor = new StandardTaskMonitor();
			CheckerboardTestUFPR evaluator = new CheckerboardTestUFPR();

			MoaStyleDynse dynse = new MoaStyleDynse(
					5,
					4,
					25,
					Constants.NUM_INST_TRAIN_CLASSIFIER_CHECKERBOARD);
			evaluator.learnerOption.setCurrentObject(dynse);

			ArffFileStream trainStream = new ArffFileStream();
			trainStream.arffFileOption.setValue(DatasetPaths.CHECKERBOARD_TRAIN);
			trainStream.prepareForUse();

			ArffFileStream testStream = new ArffFileStream();
			testStream.arffFileOption.setValue(DatasetPaths.CHECKERBOARD_TEST);
			testStream.prepareForUse();

			evaluator.setTrainStream(trainStream);
			evaluator.setTestStream(testStream);
			evaluator.prepareForUse();

			UFPRLearningCurve learningCurve = (UFPRLearningCurve) evaluator.doTask(monitor, null);
			learningCurves.add(learningCurve);
		}

		UFPRLearningCurve avgResult = ufprLearningCurveUtils.averageResults(learningCurves);
		System.out.println(ufprLearningCurveUtils.strMainStatisticsMatlab(avgResult));
	}

	private void ensureCheckerboardArffFiles() {
		if (!new File(DatasetPaths.CHECKERBOARD_TRAIN).exists()) {
			CheckerboardElwellPolikarConverter.parseFile(DatasetPaths.CHECKERBOARD_TRAIN_CSV,
					DatasetPaths.CHECKERBOARD_TRAIN_CLASS_CSV, DatasetPaths.CHECKERBOARD_TRAIN);
		}
		if (!new File(DatasetPaths.CHECKERBOARD_TEST).exists()) {
			CheckerboardElwellPolikarConverter.parseFile(DatasetPaths.CHECKERBOARD_TEST_CSV,
					DatasetPaths.CHECKERBOARD_TEST_CLASS_CSV, DatasetPaths.CHECKERBOARD_TEST);
		}
	}
}
