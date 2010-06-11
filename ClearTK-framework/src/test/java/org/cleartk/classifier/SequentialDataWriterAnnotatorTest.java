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
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkException;
import org.cleartk.FrameworkTestBase;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.SpannedTextExtractor;
import org.cleartk.classifier.jar.JarSequentialDataWriterFactory;
import org.cleartk.classifier.mallet.DefaultMalletCRFDataWriterFactory;
import org.cleartk.classifier.mallet.MalletCRFDataWriter;
import org.cleartk.test.util.TearDownUtil;
import org.cleartk.type.test.Sentence;
import org.cleartk.type.test.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.JCasUtil;
import org.junit.After;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

*/

public class SequentialDataWriterAnnotatorTest extends FrameworkTestBase {
	
	public static class TestAnnotator extends CleartkSequentialAnnotator<String> {
		
		private SimpleFeatureExtractor extractor = new SpannedTextExtractor();

		public void initialize(UimaContext context) throws ResourceInitializationException {
			super.initialize(context);
		}
		
		public void process(JCas jCas) throws AnalysisEngineProcessException {
			try {
				this.processSimple(jCas);
			} catch (CleartkException e) {
				throw new AnalysisEngineProcessException(e);
			}
		}
		
		public void processSimple(JCas jCas) throws AnalysisEngineProcessException, CleartkException {
			for (Sentence sentence: AnnotationRetrieval.getAnnotations(jCas, Sentence.class)) {
				List<Instance<String>> instances = new ArrayList<Instance<String>>();
				List<Token> tokens = AnnotationRetrieval.getAnnotations(jCas, sentence, Token.class);
				for (Token token: tokens) {
					Instance<String> instance = new Instance<String>();
					instance.addAll(this.extractor.extract(jCas, token));
					instance.setOutcome(token.getPos());
					instances.add(instance);
				}
				this.sequentialDataWriter.writeSequence(instances);
			}
		}
		
	}

	private String outputDirectory = "test/data/sequentialDataWriterAnnotator";
	@Test
	public void testSequentialDataWriterAnnotator() throws IOException, UIMAException {
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
				TestAnnotator.class, JCasUtil.getTypeSystemDescription(),
				JarSequentialDataWriterFactory.PARAM_OUTPUT_DIRECTORY, outputDirectory,
				CleartkSequentialAnnotator.PARAM_SEQUENTIAL_DATA_WRITER_FACTORY_CLASS_NAME, DefaultMalletCRFDataWriterFactory.class.getName());
		
		//create some tokens and sentences
		//add part-of-speech and stems to tokens
		
		JCas jCas = JCasUtil.getJCas();
		String text = "What if we built a large\r\n, wooden badger?";
		tokenBuilder.buildTokens(jCas, text, 
				"What if we built a large \n, wooden badger ?",
				"WDT TO PRP VBN DT JJ , JJ NN .");
		engine.process(jCas);
		engine.collectionProcessComplete();

		BufferedReader input = new BufferedReader(new FileReader(new File(outputDirectory, MalletCRFDataWriter.TRAINING_DATA_FILE_NAME)));
		String line = input.readLine();
		assertNotNull(line);
		assertTrue(line.endsWith(" WDT"));
		assertTrue(line.startsWith("What "));
		line = input.readLine();
		assertNotNull(line);
		assertTrue(line.endsWith(" TO"));
		assertTrue(line.startsWith("if "));
		line = input.readLine();
		assertNotNull(line);
		assertTrue(line.endsWith(" PRP"));
		assertTrue(line.startsWith("we "));
		line = input.readLine();
		assertNotNull(line);
		assertTrue(line.endsWith(" VBN"));
		assertTrue(line.startsWith("built "));
		line = input.readLine();
		assertNotNull(line);
		assertTrue(line.endsWith(" DT"));
		assertTrue(line.startsWith("a "));
		line = input.readLine();
		assertNotNull(line);
		assertTrue(line.endsWith(" JJ"));
		assertTrue(line.startsWith("large "));
		line = input.readLine();
		assertNotNull(line);
		assertEquals("", line.trim());
		line = input.readLine();
		assertNotNull(line);
		assertTrue(line.endsWith(" ,"));
		assertTrue(line.startsWith(", "));
		input.close();
	}
	
	@After
	public void tearDown() {
		TearDownUtil.removeDirectory(new File(outputDirectory));
	}
}
