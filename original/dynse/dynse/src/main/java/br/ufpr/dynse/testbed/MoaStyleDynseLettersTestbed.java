/*
 *    MoaStyleDynseLettersTestbed.java
 *    Testbed for running the MOA-style Dynse port on the Letter Recognition dataset.
 */
package br.ufpr.dynse.testbed;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import br.ufpr.dynse.constant.Constants;
import br.ufpr.dynse.core.MoaStyleDynse;
import br.ufpr.dynse.core.UFPRLearningCurve;
import br.ufpr.dynse.dataset.converter.LetterRecognitionConverter;
import br.ufpr.dynse.evaluation.EvaluatePeriodicHeldOutTestUFPR;
import br.ufpr.dynse.generator.PxDriftGenerator;
import br.ufpr.dynse.util.UFPRLearningCurveUtils;

public class MoaStyleDynseLettersTestbed implements MultipleExecutionsTestbed {

	private static final String PATH_DATASET = DatasetPaths.LETTERS;

	private UFPRLearningCurveUtils ufprLearningCurveUtils = new UFPRLearningCurveUtils();

	@Override
	public void executeTests(int numExec) throws Exception {
		ensureLettersArffFile();
		List<UFPRLearningCurve> learningCurves = new ArrayList<UFPRLearningCurve>(numExec);

		for (int i = 0; i < numExec; i++) {
			EvaluatePeriodicHeldOutTestUFPR evaluator = new EvaluatePeriodicHeldOutTestUFPR();

			MoaStyleDynse dynse = new MoaStyleDynse(
					5,
					32,
					25,
					Constants.NUM_INST_TRAIN_CLASSIFIER_VIRTUAL_TEST);
			evaluator.learnerOption.setCurrentObject(dynse);

			System.out.println("Executing " + i + ": MoaStyleDynseLettersKE");

			PxDriftGenerator pxDriftGenerator = new PxDriftGenerator(
					Constants.NUM_INST_TRAIN_CLASSIFIER_VIRTUAL_TEST,
					Constants.NUM_INST_TEST_CLASSIFIER_VIRTUAL_TEST,
					PATH_DATASET);

			evaluator.streamOption.setCurrentObject(pxDriftGenerator);
			evaluator.trainSizeOption.setValue(0);
			evaluator.sampleFrequencyOption.setValue(Constants.NUM_INST_TRAIN_CLASSIFIER_VIRTUAL_TEST);
			evaluator.testSizeOption.setValue(Constants.NUM_INST_TEST_CLASSIFIER_VIRTUAL_TEST);
			evaluator.prepareForUse();

			UFPRLearningCurve learningCurve = (UFPRLearningCurve) evaluator.doTask();
			learningCurves.add(learningCurve);
		}

		UFPRLearningCurve avgResult = ufprLearningCurveUtils.averageResults(learningCurves);
		System.out.println(ufprLearningCurveUtils.strMainStatisticsMatlab(avgResult));
	}

	private void ensureLettersArffFile() {
		if (!new File(PATH_DATASET).exists()) {
			LetterRecognitionConverter.parseFile(DatasetPaths.LETTERS_CSV, PATH_DATASET);
		}
	}
}
