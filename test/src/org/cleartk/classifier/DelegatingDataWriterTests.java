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
package org.cleartk.classifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.DelegatingDataWriter;
import org.cleartk.util.DocumentUtil;
import org.cleartk.util.TestsUtil;
import org.junit.After;
import org.junit.Test;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

*/

public class DelegatingDataWriterTests {

	@Test
	public void testDelegatingDataWriter() throws ResourceInitializationException, AnalysisEngineProcessException, IOException {
		AnalysisEngine engine = TestsUtil.getAnalysisEngine(
				DelegatingDataWriter.class, TestsUtil.getTypeSystem("org.cleartk.TypeSystem"),
				DelegatingDataWriter.PARAM_ANNOTATION_HANDLER, "org.cleartk.example.ExamplePOSAnnotationHandler",
				DelegatingDataWriter.PARAM_OUTPUT_DIRECTORY, "test/data/delegatingDataWriter/mallet",
				DelegatingDataWriter.PARAM_DATA_WRITER, "org.cleartk.classifier.mallet.MalletCRFDataWriter");
		
		//create some tokens and sentences
		//add part-of-speech and stems to tokens
		
		JCas jCas = engine.newJCas();
		String text = "What if we built a large\r\n, wooden badger?";
		TestsUtil.createTokens(jCas, text,
				"What if we built a large \n, wooden badger ?",
				"WDT TO PRP VBN DT JJ , JJ NN .", null);
		DocumentUtil.createDocument(jCas, "identifier", "path");
		engine.process(jCas);
		engine.collectionProcessComplete();

		BufferedReader input = new BufferedReader(new FileReader("test/data/delegatingDataWriter/mallet/training-data.malletcrf"));
		String line = input.readLine();
		assertNotNull(line);
		assertTrue(line.endsWith(" WDT"));
		assertTrue(line.startsWith("SpannedText_What "));
		line = input.readLine();
		assertNotNull(line);
		assertTrue(line.endsWith(" TO"));
		assertTrue(line.startsWith("SpannedText_if "));
		line = input.readLine();
		assertNotNull(line);
		assertTrue(line.endsWith(" PRP"));
		assertTrue(line.startsWith("SpannedText_we "));
		line = input.readLine();
		assertNotNull(line);
		assertTrue(line.endsWith(" VBN"));
		assertTrue(line.startsWith("SpannedText_built "));
		line = input.readLine();
		assertNotNull(line);
		assertTrue(line.endsWith(" DT"));
		assertTrue(line.startsWith("SpannedText_a "));
		line = input.readLine();
		assertNotNull(line);
		assertTrue(line.endsWith(" JJ"));
		assertTrue(line.startsWith("SpannedText_large "));
		line = input.readLine();
		assertNotNull(line);
		assertEquals("", line.trim());
		line = input.readLine();
		assertNotNull(line);
		assertTrue(line.endsWith(" ,"));
		assertTrue(line.startsWith("SpannedText_, "));
		input.close();
	}
	
	@After
	public void tearDown() {
		TestsUtil.tearDown(new File("test/data/delegatingDataWriter"));
	}
}
