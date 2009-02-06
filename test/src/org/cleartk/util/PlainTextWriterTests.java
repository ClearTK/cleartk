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
package org.cleartk.util;


import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.FileUtils;
import org.cleartk.util.DocumentUtil;
import org.cleartk.util.PlainTextWriter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

*/

public class PlainTextWriterTests {
	
	private final File outputDir = new File("test/data/plaintext");

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		for (File file: this.outputDir.listFiles()) {
			file.delete();
		}
		this.outputDir.delete();
	}
	
	@Test
	public void test() throws Exception {
		try {
			TestsUtil.getAnalysisEngine(
					PlainTextWriter.class,
					TestsUtil.getTypeSystem("desc/TypeSystem.xml"));
			Assert.fail("expected exception with output directory not specified");
		} catch (ResourceInitializationException e) {}
		
		AnalysisEngine engine = TestsUtil.getAnalysisEngine(
				PlainTextWriter.class, TestsUtil.getTypeSystem("desc/TypeSystem.xml"),
				PlainTextWriter.PARAM_OUTPUT_DIRECTORY, this.outputDir.getPath());
		JCas jCas = engine.newJCas();
		String text = "What if we built a large\r\n, wooden badger?";
		TestsUtil.createTokens(jCas, text,
				"What if we built a large \n, wooden badger ?",
				"WDT TO PRP VBN DT JJ , JJ NN .", null);
		DocumentUtil.createDocument(jCas, "identifier", "path");
		engine.process(jCas);
		engine.collectionProcessComplete();
		
		File outputFile = new File(this.outputDir, "identifier.txt");
		String actualText = FileUtils.file2String(outputFile);
		Assert.assertEquals(text, actualText);

		jCas = engine.newJCas();
		text = "What if we built a large\n, wooden badger?";
		TestsUtil.createTokens(jCas, text,
				"What if we built a large \n, wooden badger ?",
				"WDT TO PRP VBN DT JJ , JJ NN .", null);
		DocumentUtil.createDocument(jCas, "1234", "1234");
		engine.process(jCas);
		engine.collectionProcessComplete();
		
		outputFile = new File(this.outputDir, "1234.txt");
		actualText = FileUtils.file2String(outputFile);
		Assert.assertEquals(text, actualText);
		
	}
	
	@Test
	public void testDescriptor() throws Exception {
		try {
			TestsUtil.getAnalysisEngine("src/org/cleartk/util/PlainTextWriter.xml");
			Assert.fail("expected exception with output directory not specified");
		} catch (ResourceInitializationException e) {}

		AnalysisEngine engine = TestsUtil.getAnalysisEngine(
				"src/org/cleartk/util/PlainTextWriter.xml",
				PlainTextWriter.PARAM_OUTPUT_DIRECTORY, this.outputDir.getPath());
		engine.collectionProcessComplete();
	}

}
