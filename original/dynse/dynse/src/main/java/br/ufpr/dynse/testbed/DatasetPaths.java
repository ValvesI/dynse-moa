/*
 *    DatasetPaths.java
 *    Centralized dataset paths used by the external-file testbeds.
 */
package br.ufpr.dynse.testbed;

public final class DatasetPaths {

	private DatasetPaths() {
	}

	public static final String CHECKERBOARD = path("dynse.dataset.checkerboard",
			"DYNSE_DATASET_CHECKERBOARD", "../../../datasets/checkerboard_data/CBexponential.arff");

	public static final String CHECKERBOARD_TRAIN = path("dynse.dataset.checkerboard.train",
			"DYNSE_DATASET_CHECKERBOARD_TRAIN", "../../../datasets/checkerboard_data/CBexponentialTrain.arff");

	public static final String CHECKERBOARD_TEST = path("dynse.dataset.checkerboard.test",
			"DYNSE_DATASET_CHECKERBOARD_TEST", "../../../datasets/checkerboard_data/CBexponentialTest.arff");

	public static final String CHECKERBOARD_TRAIN_CSV = path("dynse.dataset.checkerboard.train.csv",
			"DYNSE_DATASET_CHECKERBOARD_TRAIN_CSV", "../../../datasets/checkerboard_data/CBexponential_training_data.csv");

	public static final String CHECKERBOARD_TRAIN_CLASS_CSV = path("dynse.dataset.checkerboard.train.class.csv",
			"DYNSE_DATASET_CHECKERBOARD_TRAIN_CLASS_CSV", "../../../datasets/checkerboard_data/CBexponential_training_class.csv");

	public static final String CHECKERBOARD_TEST_CSV = path("dynse.dataset.checkerboard.test.csv",
			"DYNSE_DATASET_CHECKERBOARD_TEST_CSV", "../../../datasets/checkerboard_data/CBexponential_testing_data.csv");

	public static final String CHECKERBOARD_TEST_CLASS_CSV = path("dynse.dataset.checkerboard.test.class.csv",
			"DYNSE_DATASET_CHECKERBOARD_TEST_CLASS_CSV", "../../../datasets/checkerboard_data/CBexponential_testing_class.csv");

	public static final String FOREST_COVERTYPE = path("dynse.dataset.forest",
			"DYNSE_DATASET_FOREST", "PATH_FOREST_HERE/covtypeNorm_MOA.arff");

	public static final String GAUSSIAN_TRAIN = path("dynse.dataset.gaussian.train",
			"DYNSE_DATASET_GAUSSIAN_TRAIN", "C:/datasets/gaussian_data/gaussianTrain.arff");

	public static final String GAUSSIAN_TEST = path("dynse.dataset.gaussian.test",
			"DYNSE_DATASET_GAUSSIAN_TEST", "C:/datasets/gaussian_data/gaussianTest.arff");

	public static final String GAUSSIAN_PRIORS = path("dynse.dataset.gaussian.priors",
			"DYNSE_DATASET_GAUSSIAN_PRIORS", "C:/datasets/gaussian_data/Gaussian_testing_priors.csv");

	public static final String GAUSSIAN_TRAIN_CSV = path("dynse.dataset.gaussian.train.csv",
			"DYNSE_DATASET_GAUSSIAN_TRAIN_CSV", "C:/datasets/gaussian_data/Gaussian_training_data.csv");

	public static final String GAUSSIAN_TRAIN_CLASS_CSV = path("dynse.dataset.gaussian.train.class.csv",
			"DYNSE_DATASET_GAUSSIAN_TRAIN_CLASS_CSV", "C:/datasets/gaussian_data/Gaussian_training_class.csv");

	public static final String GAUSSIAN_TEST_CSV = path("dynse.dataset.gaussian.test.csv",
			"DYNSE_DATASET_GAUSSIAN_TEST_CSV", "C:/datasets/gaussian_data/Gaussian_testing_data.csv");

	public static final String LETTERS = path("dynse.dataset.letters",
			"DYNSE_DATASET_LETTERS", "C:/datasets/letter+recognition/letters.arff");

	public static final String LETTERS_CSV = path("dynse.dataset.letters.csv",
			"DYNSE_DATASET_LETTERS_CSV", "C:/datasets/letter+recognition/letter-recognition.data");

	public static final String NEBRASKA_WEATHER = path("dynse.dataset.nebraska",
			"DYNSE_DATASET_NEBRASKA", "PATH_NEBRASKA_HERE/nebraskaWeather.arff");

	public static final String NIST = path("dynse.dataset.nist",
			"DYNSE_DATASET_NIST", "PATH_NIST_HERE/nist-norm.arff");

	private static String path(String propertyName, String environmentName, String defaultValue) {
		String propertyValue = System.getProperty(propertyName);
		if (propertyValue != null && propertyValue.trim().length() > 0) {
			return propertyValue;
		}
		String environmentValue = System.getenv(environmentName);
		if (environmentValue != null && environmentValue.trim().length() > 0) {
			return environmentValue;
		}
		return defaultValue;
	}
}
