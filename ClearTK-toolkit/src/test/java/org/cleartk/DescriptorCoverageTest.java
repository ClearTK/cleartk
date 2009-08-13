 /** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
*/
package org.cleartk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.util.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.uutuc.util.io.Files;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

*/

public class DescriptorCoverageTest {
	
	@Test
	public void testNoDescriptorsInSrcMain() {
		// collect the names of all .xml descriptors in the src directory
		Set<String> descNames = new HashSet<String>();
		for (File file: Files.getFiles(new File("src/main/java"), new String[] {".xml"})) {
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
	
	@Test
	public void testImportByLocation() throws IOException {
		List<String> filesWithLocationImport = new ArrayList<String>();
		Iterable<File> files = Files.getFiles("src", new String[] {".xml"});
		for (File file : files) {
			String fileText = FileUtils.file2String(file);
			if(fileText.indexOf("<import location=") != -1) {
				filesWithLocationImport.add(file.getPath());
			}
		}
		if (filesWithLocationImport.size() > 0) {
			String message = String.format("%d descriptor files with location imports. ", filesWithLocationImport.size());
			System.err.println(message);
			Collections.sort(filesWithLocationImport);
			for (String path : filesWithLocationImport) {
				System.err.println(path);
			}
			Assert.fail(message);
		}

	}
	
	@Test
	public void testSrcDescriptorsAreCoveredByTests() throws Exception {
		
		// collect the names of all .xml descriptors in the src directory
		Set<String> descNames = new HashSet<String>();
		for (File file: Files.getFiles(new File("src/main/resources"), new String[] {".xml"})) {
			String path = file.getPath();
			if (!path.contains("type") && !file.getParent().equals("src/org/cleartk/descriptor".replace('/', File.separatorChar))){
				// strip off "src/main/resources" and ".xml"
				String name = path.substring(19, path.length() - 4);
				// convert slashes to dots
				name = name.replace(File.separatorChar, '.');
				// add the descriptor name to the set
				descNames.add("\"" + name + "\"");
			}
		}

		// walk through every .java file in the test/src directory
		// and look for descriptor names
		for (File testFile: Files.getFiles(new File("src/test/java"), new String[] {".java"})) {
				String testText = FileUtils.file2String(testFile);
				Set<String> toRemove = new HashSet<String>();
				for (String descName: descNames) {
					if (testText.contains(descName)) {
						toRemove.add(descName);
					}
				}
				
				// have to remove the names all at once to avoid
				// ConcurrentModificationException 
				descNames.removeAll(toRemove);
		}
		
		int untested = descNames.size();
		if (untested != 0) {
			String message = String.format("%d descriptors not tested", untested);
			System.err.println(message);
			List<String> untestedFileNames = new ArrayList<String>(descNames);
			Collections.sort(untestedFileNames);
			for (String descFileName: untestedFileNames) {
				System.err.println(descFileName);
			}
			Assert.fail(message);
		}
	}
	
}
