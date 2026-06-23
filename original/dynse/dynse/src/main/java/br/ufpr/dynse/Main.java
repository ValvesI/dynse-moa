/*    
*    Main.java 
*    Copyright (C) 2017 Universidade Federal do Paraná, Curitiba, Paraná, Brasil
*    @Author Paulo Ricardo Lisboa de Almeida (prlalmeida@inf.ufpr.br)
*    This program is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License, or
*    (at your option) any later version.
*    
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*    
*    You should have received a copy of the GNU General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package br.ufpr.dynse;

import java.io.File;

import br.ufpr.dynse.testbed.CheckerboardTestbed;
import br.ufpr.dynse.testbed.DatasetPaths;
import br.ufpr.dynse.testbed.ForestCoverTypeTestBed;
import br.ufpr.dynse.testbed.GaussianTestbed;
import br.ufpr.dynse.testbed.LettersTestbed;
import br.ufpr.dynse.testbed.MultipleExecutionsTestbed;
import br.ufpr.dynse.testbed.MoaStyleDynseCheckerboardTestbed;
import br.ufpr.dynse.testbed.MoaStyleDynseForestCoverTypeTestBed;
import br.ufpr.dynse.testbed.MoaStyleDynseGaussianTestbed;
import br.ufpr.dynse.testbed.MoaStyleDynseLettersTestbed;
import br.ufpr.dynse.testbed.MoaStyleDynseSeaTestbed;
import br.ufpr.dynse.testbed.MoaStyleDynseStaggerTestbed;
import br.ufpr.dynse.testbed.SeaConceptsTestbed;
import br.ufpr.dynse.testbed.StaggerTestbed;

public class Main {
    public static void main( String[] args ) {		
		int numExecutions = 3;

//		runOptionalTestbed("Original Dynse Forest CoverType", new ForestCoverTypeTestBed(), numExecutions,
//				DatasetPaths.FOREST_COVERTYPE);
//		runOptionalTestbed("MOA-style Dynse Forest CoverType", new MoaStyleDynseForestCoverTypeTestBed(), numExecutions,
//				DatasetPaths.FOREST_COVERTYPE);
//
//		runOptionalTestbed("Original Dynse Gaussian", new GaussianTestbed(), numExecutions,
//				DatasetPaths.GAUSSIAN_TRAIN_CSV, DatasetPaths.GAUSSIAN_TRAIN_CLASS_CSV,
//				DatasetPaths.GAUSSIAN_TEST_CSV, DatasetPaths.GAUSSIAN_PRIORS);
//		runOptionalTestbed("MOA-style Dynse Gaussian", new MoaStyleDynseGaussianTestbed(), numExecutions,
//				DatasetPaths.GAUSSIAN_TRAIN_CSV, DatasetPaths.GAUSSIAN_TRAIN_CLASS_CSV,
//				DatasetPaths.GAUSSIAN_TEST_CSV, DatasetPaths.GAUSSIAN_PRIORS);
//
		runOptionalTestbed("Original Dynse Checkerboard", new CheckerboardTestbed(), numExecutions,
				DatasetPaths.CHECKERBOARD_TRAIN_CSV, DatasetPaths.CHECKERBOARD_TRAIN_CLASS_CSV,
				DatasetPaths.CHECKERBOARD_TEST_CSV, DatasetPaths.CHECKERBOARD_TEST_CLASS_CSV);
		runOptionalTestbed("MOA-style Dynse Checkerboard", new MoaStyleDynseCheckerboardTestbed(), numExecutions,
				DatasetPaths.CHECKERBOARD_TRAIN_CSV, DatasetPaths.CHECKERBOARD_TRAIN_CLASS_CSV,
				DatasetPaths.CHECKERBOARD_TEST_CSV, DatasetPaths.CHECKERBOARD_TEST_CLASS_CSV);

//		runOptionalTestbed("Original Dynse Letters", new LettersTestbed(), numExecutions,
//				DatasetPaths.LETTERS_CSV);
//		runOptionalTestbed("MOA-style Dynse Letters", new MoaStyleDynseLettersTestbed(), numExecutions,
//				DatasetPaths.LETTERS_CSV);
//
		runTestbed("Original Dynse STAGGER", new StaggerTestbed(), numExecutions);
		runTestbed("MOA-style Dynse STAGGER", new MoaStyleDynseStaggerTestbed(), numExecutions);

		runTestbed("Original Dynse SEA", new SeaConceptsTestbed(), numExecutions);
		runTestbed("MOA-style Dynse SEA", new MoaStyleDynseSeaTestbed(), numExecutions);

		System.out.println("Done!");
    }

	private static void runOptionalTestbed(String name, MultipleExecutionsTestbed testbed,
			int numExecutions, String... paths) {
		if (!pathsAvailable(paths)) {
			System.out.println("Skipping " + name + ": dataset path not configured.");
			return;
		}
		runTestbed(name, testbed, numExecutions);
	}

	private static void runTestbed(String name, MultipleExecutionsTestbed testbed, int numExecutions) {
		System.out.println("Executing " + name + " testbed...");
		try {
			testbed.executeTests(numExecutions);
		} catch (Exception e) {
			System.out.println("Failed " + name + " testbed.");
			e.printStackTrace();
		}
	}

	private static boolean pathsAvailable(String... paths) {
		for (String path : paths) {
			if (path == null || path.contains("PATH_") || !new File(path).exists()) {
				return false;
			}
		}
		return true;
    }
}
