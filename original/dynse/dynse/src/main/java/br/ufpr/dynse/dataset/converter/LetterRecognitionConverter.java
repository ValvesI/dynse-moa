/*
 *    LetterRecognitionConverter.java
 *    Converts the UCI Letter Recognition dataset to ARFF.
 */
package br.ufpr.dynse.dataset.converter;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LetterRecognitionConverter {

	private static final String[] ATTRIBUTES = {
			"x-box", "y-box", "width", "high", "onpix", "x-bar", "y-bar", "x2bar",
			"y2bar", "xybar", "x2ybr", "xy2br", "x-ege", "xegvy", "y-ege", "yegvx"
	};

	private LetterRecognitionConverter() {
	}

	public static void parseFile(String sourceCsv, String outFilePath) {
		BufferedReader reader = null;
		FileWriter writer = null;

		try {
			reader = Files.newBufferedReader(Paths.get(sourceCsv));
			writer = new FileWriter(outFilePath);
			writeHeader(writer);

			String line;
			while ((line = reader.readLine()) != null) {
				String[] values = line.split(",");
				if (values.length != 17) {
					throw new RuntimeException("Unexpected number of values in Letter Recognition line: " + line);
				}
				for (int i = 1; i < values.length; i++) {
					if (i > 1) {
						writer.write(",");
					}
					writer.write(values[i]);
				}
				writer.write(",");
				writer.write(values[0]);
				writer.write("\n");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			close(reader);
			close(writer);
		}
	}

	private static void writeHeader(FileWriter writer) throws IOException {
		writer.write("@relation LetterRecognition\n\n");
		for (String attribute : ATTRIBUTES) {
			writer.write("@attribute ");
			writer.write(attribute);
			writer.write(" numeric\n");
		}
		writer.write("@attribute class {A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z}\n\n");
		writer.write("@data\n");
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
