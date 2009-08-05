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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.util.FileUtils;
import org.cleartk.util.io.Files;
import org.junit.Assert;
import org.junit.Test;


/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 */

public class LicenseTests {

	@Test
	public void testLicenseStatedInDescriptors() throws Exception {
		testDescriptors("src");
		testDescriptors("test/src");
		testDescriptors("test/data");
		testDescriptors("cpe");
		testDescriptors("test/data");
		testDescriptors("example/desc");
	}

	private void testDescriptors(String directory) throws IOException {
		List<String> filesMissingLicense = new ArrayList<String>();
		Iterable<File> files = Files.getFiles(directory, new String[] { ".xml" });
		
		for (File file : files) {
			String fileText = FileUtils.file2String(file);

			if ((fileText.indexOf("Copyright (c) 2007-2008, Regents of the University of Colorado") == -1 &&
				fileText.indexOf("Copyright (c) 2007, Regents of the University of Colorado") == -1 &&	
				fileText.indexOf("Copyright (c) 2008, Regents of the University of Colorado") == -1 &&	
				fileText.indexOf("Copyright (c) 2009, Regents of the University of Colorado") == -1 &&	
				fileText.indexOf("Copyright (c) 2007-2009, Regents of the University of Colorado") == -1)	
					|| fileText.indexOf("THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\"") == -1) {
				
				if(file.getName().equals("GENIAcorpus3.02.articleA.pos.xml"))
					continue;
				if(file.getParent().equals("src/org/cleartk/descriptor".replace('/', File.separatorChar)))
					continue;
				filesMissingLicense.add(file.getPath());
			}
		}
		
		if (filesMissingLicense.size() > 0) {
			String message = String.format("%d descriptor files with no license. ", filesMissingLicense.size());
			System.err.println(message);
			Collections.sort(filesMissingLicense);
			for (String path : filesMissingLicense) {
				System.err.println(path);
			}
			Assert.fail(message);
		}
	}
	
	@Test
	public void testLicenseStatedInSource() throws Exception {
		test("src");
	}

	@Test
	public void testLicenseStatedInTestSource() throws Exception {
		test("test/src");

	}

	private void test(String directory) throws IOException {
		List<String> filesMissingLicense = new ArrayList<String>();
		Iterable<File> files = Files.getFiles(directory, new String[] { ".java" });
		
		for (File file : files) {
			String fileText = FileUtils.file2String(file);

			if (file.getParentFile().getName().equals("type") || file.getName().equals("Files.java")
					|| file.getParentFile().getName().equals("types")
					|| file.getParentFile().getName().equals("pubmed")
					|| file.getName().equals("XWriter.java")) {
				continue;
			}

			
			if ((fileText.indexOf("Copyright (c) 2007-2008, Regents of the University of Colorado") == -1 &&
					fileText.indexOf("Copyright (c) 2007, Regents of the University of Colorado") == -1 &&	
					fileText.indexOf("Copyright (c) 2008, Regents of the University of Colorado") == -1 &&	
					fileText.indexOf("Copyright (c) 2009, Regents of the University of Colorado") == -1 &&	
					fileText.indexOf("Copyright (c) 2007-2009, Regents of the University of Colorado") == -1)	
					|| fileText.indexOf("THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\"") == -1) {
				filesMissingLicense.add(file.getPath());
			}
			else {
				int index = 300;
				if ((fileText.indexOf("Copyright (c) 2007-2008, Regents of the University of Colorado", index) == -1 &&
						fileText.indexOf("Copyright (c) 2007, Regents of the University of Colorado") == -1 &&	
						fileText.indexOf("Copyright (c) 2008, Regents of the University of Colorado") == -1 &&	
						fileText.indexOf("Copyright (c) 2009, Regents of the University of Colorado") == -1 &&	
						fileText.indexOf("Copyright (c) 2007-2009, Regents of the University of Colorado", index) == -1))	
					filesMissingLicense.add(file.getPath());
			}

		}

		if (filesMissingLicense.size() > 0) {
			String message = String.format("%d source files with no license. ", filesMissingLicense.size());
			System.err.println(message);
			Collections.sort(filesMissingLicense);
			for (String path : filesMissingLicense) {
				System.err.println(path);
			}
			Assert.fail(message);
		}

	}

	public static void main(String[] args) throws IOException {

		for (File directory : new File[] { new File("src"), new File("test/src") }) {
			Iterable<File> files = Files.getFiles(directory, new String[] { ".java" });
			;

			String oldLicenseRegex = " ?\\* [C]opyright 2007-2008 Regents of the University of Colorado.*CU Research License Agreement</a>(<p>)?";
			Pattern oldLicensePattern = Pattern.compile(oldLicenseRegex, Pattern.DOTALL | Pattern.MULTILINE);

			String newline = System.getProperty("line.separator");

			String textLicense = " /** "
					+ newline
					+ " * Copyright (c) 2007-2008, Regents of the University of Colorado "
					+ newline
					+ " * All rights reserved."
					+ newline
					+ " * "
					+ newline
					+ " * Redistribution and use in source and binary forms, with or without"
					+ newline
					+ " * modification, are permitted provided that the following conditions are met:"
					+ newline
					+ " * "
					+ newline
					+ " * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. "
					+ newline
					+ " * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. "
					+ newline
					+ " * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. "
					+ newline + " * " + newline
					+ " * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\"" + newline
					+ " * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE" + newline
					+ " * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE" + newline
					+ " * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE" + newline
					+ " * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR" + newline
					+ " * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF" + newline
					+ " * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS" + newline
					+ " * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN" + newline
					+ " * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)" + newline
					+ " * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE" + newline
					+ " * POSSIBILITY OF SUCH DAMAGE. " + newline + "*/" + newline;

			String copyright = " * <br>Copyright (c) 2007-2008, Regents of the University of Colorado " + newline
					+ " * <br>All rights reserved." + newline;

			Matcher matcher;
			for (File file : files) {
				String fileText = FileUtils.file2String(file);

				if (file.getParentFile().getName().equals("type") || file.getName().equals("Files.java")
						|| file.getParentFile().getName().equals("types") || file.getName().equals("LicenseTests.java")
						|| file.getParentFile().getName().equals("pubmed")) {
					continue;
				}

				matcher = oldLicensePattern.matcher(fileText);
				if (matcher.find()) {
					fileText = textLicense + matcher.replaceFirst(copyright);
					System.out.println(file.getName());
				}
				else {
					System.out.println(file.getName());
				}

				FileUtils.saveString2File(fileText, file);

			}
		}

	}
}

