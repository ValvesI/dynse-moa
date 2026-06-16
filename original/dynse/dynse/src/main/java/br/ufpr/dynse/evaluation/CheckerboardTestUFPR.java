/*
 *    CheckerboardTestUFPR.java
 *    Batch evaluator for the Elwell and Polikar checkerboard datasets.
 */
package br.ufpr.dynse.evaluation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import br.ufpr.dynse.core.UFPRLearningCurve;
import moa.classifiers.Classifier;
import moa.core.Example;
import moa.core.Measurement;
import moa.core.ObjectRepository;
import moa.core.StringUtils;
import moa.core.TimingUtils;
import moa.evaluation.LearningPerformanceEvaluator;
import moa.evaluation.preview.LearningCurve;
import moa.streams.InstanceStream;
import moa.tasks.EvaluatePeriodicHeldOutTest;
import moa.tasks.TaskMonitor;

public class CheckerboardTestUFPR extends EvaluatePeriodicHeldOutTest {

	private static final long serialVersionUID = 1L;

	private static final int TRAIN_BATCH_SIZE = 25;
	private static final int TEST_BATCH_SIZE = 1024;

	private InstanceStream trainStream;
	private InstanceStream testStream;

	public void setTrainStream(InstanceStream trainStream) {
		this.trainStream = trainStream;
	}

	public void setTestStream(InstanceStream testStream) {
		this.testStream = testStream;
	}

	@Override
	protected Object doMainTask(TaskMonitor monitor, ObjectRepository repository) {
		Classifier learner = (Classifier) getPreparedClassOption(this.learnerOption);
		LearningPerformanceEvaluator evaluator = (LearningPerformanceEvaluator) getPreparedClassOption(this.evaluatorOption);
		learner.setModelContext(trainStream.getHeader());

		long instancesProcessed = 0;
		UFPRLearningCurve learningCurve = new UFPRLearningCurve("evaluation instances");
		File dumpFile = this.dumpFileOption.getFile();
		PrintStream immediateResultStream = null;
		if (dumpFile != null) {
			try {
				immediateResultStream = new PrintStream(new FileOutputStream(dumpFile, dumpFile.exists()), true);
			} catch (Exception ex) {
				throw new RuntimeException("Unable to open immediate result file: " + dumpFile, ex);
			}
		}
		boolean firstDump = true;

		TimingUtils.enablePreciseTiming();
		double totalTrainTime = 0.0;
		while (trainStream.hasMoreInstances() && testStream.hasMoreInstances()) {
			monitor.setCurrentActivityDescription("Training...");
			long trainStartTime = TimingUtils.getNanoCPUTimeOfCurrentThread();
			int trainCount = 0;
			while (trainCount < TRAIN_BATCH_SIZE && trainStream.hasMoreInstances()) {
				learner.trainOnInstance(trainStream.nextInstance());
				trainCount++;
				instancesProcessed++;
			}
			if (trainCount != TRAIN_BATCH_SIZE) {
				break;
			}
			double lastTrainTime = TimingUtils.nanoTimeToSeconds(TimingUtils.getNanoCPUTimeOfCurrentThread()
					- trainStartTime);
			totalTrainTime += lastTrainTime;

			evaluator.reset();
			monitor.setCurrentActivityDescription("Testing (after "
					+ StringUtils.doubleToString(instancesProcessed, 2) + " training instances)...");
			long testStartTime = TimingUtils.getNanoCPUTimeOfCurrentThread();
			int testCount = 0;
			while (testCount < TEST_BATCH_SIZE && testStream.hasMoreInstances()) {
				Example testInstance = testStream.nextInstance();
				double[] prediction = learner.getVotesForInstance(testInstance);
				evaluator.addResult(testInstance, prediction);
				testCount++;
			}
			if (testCount != TEST_BATCH_SIZE) {
				break;
			}
			double testTime = TimingUtils.nanoTimeToSeconds(TimingUtils.getNanoCPUTimeOfCurrentThread()
					- testStartTime);

			List<Measurement> measurements = new ArrayList<Measurement>();
			measurements.add(new Measurement("evaluation instances", instancesProcessed));
			measurements.add(new Measurement("total train time", totalTrainTime));
			measurements.add(new Measurement("total train speed", instancesProcessed / totalTrainTime));
			measurements.add(new Measurement("last train time", lastTrainTime));
			measurements.add(new Measurement("last train speed", TRAIN_BATCH_SIZE / lastTrainTime));
			measurements.add(new Measurement("test time", testTime));
			measurements.add(new Measurement("test speed", TEST_BATCH_SIZE / testTime));

			Measurement[] performanceMeasurements = evaluator.getPerformanceMeasurements();
			Measurement classifiedInstances = performanceMeasurements[0];
			Measurement accuracy = performanceMeasurements[1];
			for (int i = 2; i < performanceMeasurements.length; i++) {
				measurements.add(performanceMeasurements[i]);
			}
			for (Measurement measurement : learner.getModelMeasurements()) {
				measurements.add(measurement);
			}

			learningCurve.insertEntry(accuracy, classifiedInstances, measurements);
			if (immediateResultStream != null) {
				if (firstDump) {
					immediateResultStream.println(learningCurve.headerToString());
					firstDump = false;
				}
				immediateResultStream.println(learningCurve.entryToString(learningCurve.numEntries() - 1));
				immediateResultStream.flush();
			}
			if (monitor.resultPreviewRequested()) {
				monitor.setLatestResultPreview(learningCurve.copy());
			}
		}

		if (immediateResultStream != null) {
			immediateResultStream.close();
		}
		return learningCurve;
	}

	@Override
	public Class<?> getTaskResultType() {
		return LearningCurve.class;
	}
}
