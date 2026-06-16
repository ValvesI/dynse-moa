/*
 *    CheckerboardElwellPolikarConverter.java
 *    Converts the original Elwell and Polikar checkerboard CSV files to an
 *    interleaved ARFF stream compatible with EvaluatePeriodicHeldOutTestUFPR.
 */
package br.ufpr.dynse.dataset.converter;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CheckerboardElwellPolikarConverter {

	private static final int TRAIN_BATCH_SIZE = 25;
	private static final int TEST_BATCH_SIZE = 1024;

	private static final String HEADER = "@relation RotatingCheckerboard\n\n"
			+ "@attribute gridX numeric\n"
			+ "@attribute gridY numeric\n"
			+ "@attribute class {1,2}\n\n"
			+ "@data\n";

	private CheckerboardElwellPolikarConverter() {
	}

	public static void parseInterleavedFile(String trainData, String trainClass,
			String testData, String testClass, String outFilePath) {
		BufferedReader trainDataReader = null;
		BufferedReader trainClassReader = null;
		BufferedReader testDataReader = null;
		BufferedReader testClassReader = null;
		FileWriter writer = null;

		try {
			trainDataReader = Files.newBufferedReader(Paths.get(trainData));
			trainClassReader = Files.newBufferedReader(Paths.get(trainClass));
			testDataReader = Files.newBufferedReader(Paths.get(testData));
			testClassReader = Files.newBufferedReader(Paths.get(testClass));
			writer = new FileWriter(outFilePath);

			writer.write(HEADER);

			while (true) {
				if (!writeBatch(trainDataReader, trainClassReader, writer, TRAIN_BATCH_SIZE)) {
					break;
				}
				if (!writeBatch(testDataReader, testClassReader, writer, TEST_BATCH_SIZE)) {
					break;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			close(trainDataReader);
			close(trainClassReader);
			close(testDataReader);
			close(testClassReader);
			close(writer);
		}
	}

	public static void parseFile(String csvInstances, String csvLabels, String outFilePath) {
		BufferedReader instancesReader = null;
		BufferedReader labelsReader = null;
		FileWriter writer = null;

		try {
			instancesReader = Files.newBufferedReader(Paths.get(csvInstances));
			labelsReader = Files.newBufferedReader(Paths.get(csvLabels));
			writer = new FileWriter(outFilePath);

			writer.write(HEADER);

			String instanceLine;
			while ((instanceLine = instancesReader.readLine()) != null) {
				String labelLine = labelsReader.readLine();
				if (labelLine == null) {
					break;
				}
				writer.write(instanceLine);
				writer.write(",");
				writer.write(labelLine);
				writer.write("\n");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			close(instancesReader);
			close(labelsReader);
			close(writer);
		}
	}

	private static boolean writeBatch(BufferedReader dataReader, BufferedReader classReader,
			FileWriter writer, int batchSize) throws IOException {
		for (int i = 0; i < batchSize; i++) {
			String dataLine = dataReader.readLine();
			String classLine = classReader.readLine();
			if (dataLine == null || classLine == null) {
				return false;
			}
			writer.write(dataLine);
			writer.write(",");
			writer.write(classLine);
			writer.write("\n");
		}
		return true;
	}

	private static void close(AutoCloseable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
