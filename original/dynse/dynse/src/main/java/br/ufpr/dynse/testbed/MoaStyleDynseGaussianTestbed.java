/*
 *    MoaStyleDynseGaussianTestbed.java
 *    Testbed for running the MOA-style Dynse port on the Gaussian dataset.
 */
package br.ufpr.dynse.testbed;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import br.ufpr.dynse.constant.Constants;
import br.ufpr.dynse.core.MoaStyleDynse;
import br.ufpr.dynse.core.UFPRLearningCurve;
import br.ufpr.dynse.dataset.converter.GaussianElwellPolikarConverter;
import br.ufpr.dynse.evaluation.GaussianTestUFPR;
import br.ufpr.dynse.util.UFPRLearningCurveUtils;
import moa.streams.ArffFileStream;
import moa.tasks.StandardTaskMonitor;
import moa.tasks.TaskMonitor;

public class MoaStyleDynseGaussianTestbed implements MultipleExecutionsTestbed {

	private final UFPRLearningCurveUtils ufprLearningCurveUtils = new UFPRLearningCurveUtils();

	private static final String PATH_TRAIN_FILE = DatasetPaths.GAUSSIAN_TRAIN;
	private static final String PATH_TEST_FILE = DatasetPaths.GAUSSIAN_TEST;
	private static final String PATH_PRIORS = DatasetPaths.GAUSSIAN_PRIORS;

	@Override
	public void executeTests(int numberExec) throws Exception {
		ensureGaussianArffFiles();

		List<UFPRLearningCurve> learningCurves = new ArrayList<UFPRLearningCurve>(numberExec);
		for (int i = 0; i < numberExec; i++) {
			System.out.println("Executing MOA-style Dynse Gaussian KE - Exec.: " + i);
			TaskMonitor monitor = new StandardTaskMonitor();
			GaussianTestUFPR evaluator = new GaussianTestUFPR();

			MoaStyleDynse dynse = new MoaStyleDynse(
					5,
					4,
					25,
					Constants.NUM_INST_TRAIN_GAUSS_POLIKAR);
			evaluator.learnerOption.setCurrentObject(dynse);

			ArffFileStream trainStream = new ArffFileStream();
			trainStream.arffFileOption.setValue(PATH_TRAIN_FILE);
			trainStream.prepareForUse();

			ArffFileStream testStream = new ArffFileStream();
			testStream.arffFileOption.setValue(PATH_TEST_FILE);
			testStream.prepareForUse();

			List<Double[]> priors = GaussianElwellPolikarConverter.readPriorsTest(PATH_PRIORS);

			evaluator.setTrainStream(trainStream);
			evaluator.setTestStream(testStream);
			evaluator.setPriors(priors);
			evaluator.prepareForUse();

			UFPRLearningCurve learningCurve = (UFPRLearningCurve) evaluator.doTask(monitor, null);
			learningCurves.add(learningCurve);
		}

		UFPRLearningCurve avgResult = ufprLearningCurveUtils.averageResults(learningCurves);
		System.out.println(ufprLearningCurveUtils.strMainStatisticsMatlab(avgResult));
	}

	private void ensureGaussianArffFiles() {
		if (!new File(PATH_TRAIN_FILE).exists()) {
			GaussianElwellPolikarConverter.parseTrainFile(DatasetPaths.GAUSSIAN_TRAIN_CSV,
					DatasetPaths.GAUSSIAN_TRAIN_CLASS_CSV, PATH_TRAIN_FILE);
		}
		if (!new File(PATH_TEST_FILE).exists()) {
			GaussianElwellPolikarConverter.parseTestFile(DatasetPaths.GAUSSIAN_TEST_CSV, PATH_TEST_FILE);
		}
	}
}
