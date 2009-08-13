 /** 
 * Copyright (c) 2007-2009, Regents of the University of Colorado 
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
import java.util.List;

import org.apache.uima.pear.util.FileUtil;
import org.junit.Assert;
import org.junit.Test;
import org.uutuc.util.io.Files;

/**
 * <br>Copyright (c) 2007-2009, Regents of the University of Colorado 
 * <br>All rights reserved.
 *
 * @author Philip Ogren
 */
public class SysoutTest {

	@Test
	public void testSysout() throws IOException {
		List<String> violatingFiles = new ArrayList<String>();
		Iterable<File> files = Files.getFiles("src/main", new String[] { ".java" });

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
