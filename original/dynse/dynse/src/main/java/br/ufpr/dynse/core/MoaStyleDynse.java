/*
 *    MoaStyleDynse.java
 *    Port of the MOA-style Dynse implementation for comparison inside the
 *    original Dynse project testbed.
 */
package br.ufpr.dynse.core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;

import moa.capabilities.CapabilitiesHandler;
import moa.capabilities.Capability;
import moa.capabilities.ImmutableCapabilities;
import moa.classifiers.AbstractClassifier;
import moa.classifiers.Classifier;
import moa.classifiers.MultiClassClassifier;
import moa.classifiers.lazy.neighboursearch.LinearNNSearch;
import moa.classifiers.trees.HoeffdingTree;
import moa.core.Measurement;
import moa.core.SizeOf;
import moa.core.StringUtils;

public class MoaStyleDynse extends AbstractClassifier implements MultiClassClassifier, CapabilitiesHandler {

	private static final long serialVersionUID = 1L;

	private final int k;
	private final int maxWindowBatches;
	private final int maxPoolSize;
	private final int trainBatchSize;

	private List<Classifier> pool;
	private Instances buffer;
	private Instances accuracyEstimationWindow;
	private boolean instantPrune;

	public MoaStyleDynse(int trainBatchSize) {
		this(5, 4, 25, trainBatchSize);
	}

	public MoaStyleDynse(int k, int maxWindowBatches, int maxPoolSize, int trainBatchSize) {
		this.k = k;
		this.maxWindowBatches = maxWindowBatches;
		this.maxPoolSize = maxPoolSize;
		this.trainBatchSize = trainBatchSize;
		this.instantPrune = false;
		resetLearning();
	}

	@Override
	public void resetLearningImpl() {
		this.pool = new ArrayList<Classifier>();
		this.buffer = null;
		this.accuracyEstimationWindow = new Instances();
	}

	@Override
	public void trainOnInstanceImpl(Instance instance) {
		if (buffer == null) {
			buffer = new Instances(instance.dataset(), 0);
		}

		// Removes the temporary incomplete classifier from last iteration
		if(instantPrune) {
			this.pool.removeLast();
		}

		if(!(accuracyEstimationWindow.numInstances() == 0)) {
			if(accuracyEstimationWindow.numInstances() > trainBatchSize) {
				accuracyEstimationWindow.delete(0);
			}
			accuracyEstimationWindow.add(instance);
			buffer.add(instance);

		} else {
			accuracyEstimationWindow.add(instance);
			buffer.add(instance);
		}

		if(buffer.numInstances() == trainBatchSize) {
			Classifier newClassifier = TrainClassifier(buffer);
			pruneByAgeAndAdd(newClassifier);
			buffer.delete();
		} else {
			Classifier newClassifier = TrainClassifier(buffer);
			this.pool.add(newClassifier);
			instantPrune = true;
		}


		if (buffer.numInstances() >= trainBatchSize) {
			Classifier newClassifier = new HoeffdingTree();
			newClassifier.prepareForUse();
			newClassifier.resetLearning();
			for (int i = 0; i < buffer.numInstances(); i++) {
				newClassifier.trainOnInstance(buffer.instance(i));
			}

			accuracyEstimationWindow.add(new Instances(buffer));
			if (accuracyEstimationWindow.size() > maxWindowBatches) {
				accuracyEstimationWindow.removeFirst();
			}

			pruneByAgeAndAdd(newClassifier);
			buffer.delete();
		}
	}

	@Override
	public double[] getVotesForInstance(Instance instance) {
		if (pool == null || pool.isEmpty() || accuracyEstimationWindow == null
				|| accuracyEstimationWindow.isEmpty()) {
			return new double[instance.numClasses()];
		}

		Instances windowInstances = flattenWindow();
		if (windowInstances.numInstances() == 0) {
			return new double[instance.numClasses()];
		}

		Instances neighbours;
		try {
			LinearNNSearch search = new LinearNNSearch(windowInstances);
			neighbours = search.kNearestNeighbours(instance, Math.min(k, windowInstances.numInstances()));
		} catch (Exception e) {
			return new double[instance.numClasses()];
		}

		List<Classifier> competent = selectCompetentClassifiers(neighbours);
		return combineVotes(competent, instance);
	}

	private void pruneByAgeAndAdd(Classifier newClassifier) {
		if (pool.size() >= maxPoolSize) {
			pool.remove(0);
		}
		pool.add(newClassifier);
	}

	private Instances flattenWindow() {
		Instances result = new Instances(accuracyEstimationWindow.getFirst().instance(0).dataset(), 0);
		for (Instances batch : accuracyEstimationWindow) {
			for (int i = 0; i < batch.numInstances(); i++) {
				result.add(batch.instance(i));
			}
		}
		return result;
	}

	private List<Classifier> selectCompetentClassifiers(Instances neighbours) {
		int slack = 2;
		List<Classifier> competent = new ArrayList<Classifier>();

		while (competent.isEmpty() && slack <= k) {
			for (Classifier classifier : pool) {
				if (isCompetent(classifier, neighbours, slack)) {
					competent.add(classifier);
				}
			}
			slack++;
		}
		return competent;
	}

	private boolean isCompetent(Classifier classifier, Instances neighbours, int slack) {
		int errors = 0;
		for (int i = 0; i < neighbours.numInstances(); i++) {
			Instance neighbour = neighbours.instance(i);
			double[] votes = classifier.getVotesForInstance(neighbour);
			if (argMax(votes) != (int) neighbour.classValue()) {
				errors++;
			}
		}
		return errors <= slack;
	}

	private double[] combineVotes(List<Classifier> competent, Instance instance) {
		double[] combined = new double[instance.numClasses()];
		if (competent.isEmpty()) {
			//combined[ThreadLocalRandom.current().nextInt(combined.length)] = 1.0;
			combined[0] = 1.0;
			return combined;
		}
		for (Classifier classifier : competent) {
			double[] votes = classifier.getVotesForInstance(instance);
			if (votes.length > 0) {
				combined[argMax(votes)]++;
			}
		}
		return combined;
	}

	private int argMax(double[] votes) {
		int best = 0;
		for (int i = 1; i < votes.length; i++) {
			if (votes[i] > votes[best]) {
				best = i;
			}
		}
		return best;
	}

	@Override
	protected Measurement[] getModelMeasurementsImpl() {
		return new Measurement[] {
				new Measurement("pool size", pool != null ? pool.size() : 0),
				new Measurement("accuracy estimation window size",
						accuracyEstimationWindow != null ? accuracyEstimationWindow.size() : 0),
				new Measurement("buffer size", buffer != null ? buffer.numInstances() : 0)
		};
	}

	@Override
	public int measureByteSize() {
		int byteSize = (int) SizeOf.sizeOf(this);
		if (pool != null) {
			for (Classifier classifier : pool) {
				if (classifier != null) {
					byteSize += classifier.measureByteSize();
				}
			}
		}
		if (accuracyEstimationWindow != null) {
			byteSize += (int) SizeOf.fullSizeOf(accuracyEstimationWindow);
		}
		if (buffer != null) {
			byteSize += (int) SizeOf.fullSizeOf(buffer);
		}
		return byteSize;
	}

	@Override
	public void getModelDescription(StringBuilder out, int indent) {
		StringUtils.appendIndented(out, indent, "MOA-style Dynse port");
		StringUtils.appendNewline(out);
		StringUtils.appendIndented(out, indent, "k: ");
		out.append(k);
		StringUtils.appendNewline(out);
		StringUtils.appendIndented(out, indent, "max window batches: ");
		out.append(maxWindowBatches);
		StringUtils.appendNewline(out);
		StringUtils.appendIndented(out, indent, "max pool size: ");
		out.append(maxPoolSize);
		StringUtils.appendNewline(out);
		StringUtils.appendIndented(out, indent, "train batch size: ");
		out.append(trainBatchSize);
		StringUtils.appendNewline(out);
	}

	@Override
	public Classifier[] getSubClassifiers() {
		return pool != null ? pool.toArray(new Classifier[0]) : new Classifier[0];
	}

	@Override
	public boolean isRandomizable() {
		return false;
	}

	@Override
	public ImmutableCapabilities defineImmutableCapabilities() {
		return new ImmutableCapabilities(Capability.VIEW_STANDARD, Capability.VIEW_LITE);
	}

	private Classifier TrainClassifier(Instances batch) {
		//TODO quando for implementar isso no padrão MOA HoeffdingTree deve ser o baseClassifier
		Classifier newClassifier = new HoeffdingTree();
		newClassifier.prepareForUse();
		newClassifier.resetLearning();
		for (int i = 0; i < buffer.numInstances(); i++) {
			newClassifier.trainOnInstance(buffer.instance(i));
		}
		return newClassifier;
	}

}
