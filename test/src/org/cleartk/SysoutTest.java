package org.cleartk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.pear.util.FileUtil;
import org.cleartk.util.io.Files;
import org.junit.Assert;
import org.junit.Test;

public class SysoutTest {

	@Test
	public void testSysout() throws IOException {
		List<String> violatingFiles = new ArrayList<String>();
		Iterable<File> files = Files.getFiles("src", new String[] { ".java" });

		for (File file : files) {
			if(file.getName().equals("BuildJar.java") ||
					file.getName().equals("Train.java") ||
					file.getName().equals("RunCPE.java") ||
					file.getName().equals("BinaryLIBSVMClassifierBuilder.java") ||
					file.getName().equals("MultiClassLIBSVMClassifierBuilder.java") ||
					file.getName().equals("MaxentEval.java") ||
					file.getName().equals("SVMlightClassifierBuilder.java") ||
					file.getName().equals("Evaluator.java") ||
					file.getName().equals("GeniaPOSParser.java") ||
					file.getName().equals("RunExamplePOSAnnotator.java") ) {
				continue;
			}
			String[] lines = FileUtil.loadListOfStrings(file);
			for(String line : lines) {
				line = line.trim();
				if(line.indexOf("System.out") != -1 ||
				line.indexOf("System.err") != -1) {
					if(!line.startsWith("//")) {
						violatingFiles.add(file.getPath());
						break;
					}
			}
			}			
		}

		if (violatingFiles.size() > 0) {
			String message = String.format("%d files contain System.out or System.err calls. ", violatingFiles.size());
			System.err.println(message);
			for (String violatingFile : violatingFiles) {
				System.err.println(violatingFile);
			}
			Assert.fail(message);
		}

	}

}
