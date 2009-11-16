package org.cleartk.test.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.uutuc.util.io.Files;

public class DescriptorCoverageTestUtil {

	public static void testNoDescriptorsInSrc(String srcDirectoryName) {
		// collect the names of all .xml descriptors in the src directory
		Set<String> descNames = new HashSet<String>();
		for (File file: Files.getFiles(new File(srcDirectoryName), new String[] {".xml"})) {
			descNames.add(file.getPath());
		}
		
		if(descNames.size() > 0) {
			String message = String.format("%d descriptors in src/main/java", descNames.size());
			System.err.println(message);
			List<String> sortedFileNames = new ArrayList<String>(descNames);
			Collections.sort(sortedFileNames);
			for (String name: sortedFileNames) {
				System.err.println(name);
			}
			Assert.fail(message);
		}
		
	}

}
