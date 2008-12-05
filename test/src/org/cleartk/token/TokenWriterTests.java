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
package org.cleartk.token;


import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.FileUtils;
import org.cleartk.token.TokenWriter;
import org.cleartk.util.DocumentUtil;
import org.cleartk.util.TestsUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

*/

public class TokenWriterTests {
	
	private final File outputDir = new File("test/data/tokenwriter");

	@After
	public void tearDown() throws Exception {
		File[] files = this.outputDir.listFiles();
		if (files != null) {
			for (File file: this.outputDir.listFiles()) {
				file.delete();
			}
		}
		this.outputDir.delete();
	}
	
	@Test
	public void testMissingParameter() throws Exception {
		try {
			TestsUtil.getAnalysisEngine(
					TokenWriter.class, TestsUtil.getTypeSystem("desc/TypeSystem.xml"));
			Assert.fail("expected error with no output directory specified");
		} catch (ResourceInitializationException e) {}
	}
	@Test
	public void testOutputFile() throws Exception {
		AnalysisEngine engine = TestsUtil.getAnalysisEngine(
				TokenWriter.class, TestsUtil.getTypeSystem("desc/TypeSystem.xml"),
				TokenWriter.PARAM_OUTPUT_DIRECTORY, this.outputDir.getPath());
		
		JCas jCas = engine.newJCas();
		String spacedTokens = "What if we built a large , wooden badger ?\nHmm? ";
		TestsUtil.createTokens(jCas,
				"What if we built\na large, wooden badger? Hmm?", 
				spacedTokens, null, null);
		DocumentUtil.createDocument(jCas, "identifier", "path");
		engine.process(jCas);
		engine.collectionProcessComplete();
		
		String expected = spacedTokens.replace("\n", "\n\n").replace(' ', '\n');
		File outputFile = new File(this.outputDir, "identifier.txt");
		String actual = FileUtils.file2String(outputFile).replace("\r", "");
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testDescriptor() throws UIMAException, IOException {
		try {
			TestsUtil.getAnalysisEngine("desc/token/TokenWriter.xml");
			Assert.fail("expected exception with no output directory specified");
		} catch (ResourceInitializationException e) {}
		
		AnalysisEngine engine = TestsUtil.getAnalysisEngine(
				"desc/token/TokenWriter.xml",
				TokenWriter.PARAM_OUTPUT_DIRECTORY, this.outputDir.getPath());
		
		Object outputDir = engine.getConfigParameterValue(
				TokenWriter.PARAM_OUTPUT_DIRECTORY);
		Assert.assertEquals(this.outputDir.getPath(), outputDir);
		
		engine.collectionProcessComplete();
	}
}
