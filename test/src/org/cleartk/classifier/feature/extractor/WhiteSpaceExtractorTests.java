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
package org.cleartk.classifier.feature.extractor;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;
import org.cleartk.type.Token;
import org.junit.Test;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.TypeSystemDescriptionFactory;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

*/

public class WhiteSpaceExtractorTests {

	public static class Annotator extends JCasAnnotator_ImplBase {

		/**
		 * Assumes that we are processing the text: "This is some test text."
		 */

		public void process(JCas jCas) throws AnalysisEngineProcessException {
			// This
			Token t1 = new Token(jCas, 0, 4);
			t1.addToIndexes();
			// is
			Token t2 = new Token(jCas, 5, 7);
			t2.addToIndexes();
			// some
			Token t3 = new Token(jCas, 8, 12);
			t3.addToIndexes();
			// test
			Token t4 = new Token(jCas, 13, 17);
			t4.addToIndexes();
			// text
			Token t5 = new Token(jCas, 18, 22);
			t5.addToIndexes();
			// .
			Token t6 = new Token(jCas, 22, 23);
			t6.addToIndexes();
			// me
			Token t7 = new Token(jCas, 10, 12);
			t7.addToIndexes();
			// st te
			Token t8 = new Token(jCas, 15, 20);
			t8.addToIndexes();
			// This is some test text.
			Token t9 = new Token(jCas, 0, 23);
			t9.addToIndexes();

		}
	}

	@Test
	public void testExtract() throws Exception{
			AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
					WhiteSpaceExtractorTests.Annotator.class,
					TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"));
			JCas jCas = AnalysisEngineFactory.process(engine,"This is some test text.");
			FSIndex fsIndex = jCas.getAnnotationIndex(Token.type);

			Token targetToken = new Token(jCas, 0, 4);
			Token t1 = (Token) fsIndex.find(targetToken);
			WhiteSpaceExtractor extractor = new WhiteSpaceExtractor();
			List<Feature> features = extractor.extract(jCas, t1);
			assertEquals(2, features.size());
			Feature feature = features.get(0);
			assertEquals(WhiteSpaceExtractor.ORIENTATION_LEFT, feature.getValue()); 
			feature = features.get(1);
			assertEquals(WhiteSpaceExtractor.ORIENTATION_RIGHT, feature.getValue());
			assertEquals("whitespace", feature.getName());

			targetToken = new Token(jCas, 5, 7);
			t1 = (Token) fsIndex.find(targetToken);
			features = extractor.extract(jCas, t1);
			assertEquals(features.size(), 2);
			feature = features.get(0);
			assertEquals(WhiteSpaceExtractor.ORIENTATION_LEFT, feature.getValue());
			feature = features.get(1);
			assertEquals(WhiteSpaceExtractor.ORIENTATION_RIGHT, feature.getValue());
			assertEquals("whitespace", feature.getName());

			targetToken = new Token(jCas, 18, 22);
			t1 = (Token) fsIndex.find(targetToken);
			features = extractor.extract(jCas, t1);
			assertEquals(features.size(), 1);
			feature = features.get(0);
			assertEquals(WhiteSpaceExtractor.ORIENTATION_LEFT, feature.getValue());

			targetToken = new Token(jCas, 22, 23);
			t1 = (Token) fsIndex.find(targetToken);
			features = extractor.extract(jCas, t1);
			assertEquals(1, features.size());
			feature = features.get(0);
			assertEquals(WhiteSpaceExtractor.ORIENTATION_RIGHT, feature.getValue());
			
			targetToken = new Token(jCas, 10, 12);
			t1 = (Token) fsIndex.find(targetToken);
			features = extractor.extract(jCas, t1);
			assertEquals(1, features.size());
			feature = features.get(0);
			assertEquals(WhiteSpaceExtractor.ORIENTATION_RIGHT, feature.getValue());
			
			targetToken = new Token(jCas, 15, 20);
			t1 = (Token) fsIndex.find(targetToken);
			features = extractor.extract(jCas, t1);
			assertEquals(0, features.size());

			targetToken = new Token(jCas, 0, 23);
			t1 = (Token) fsIndex.find(targetToken);
			features = extractor.extract(jCas, t1);
			assertEquals(features.size(), 2);
			feature = features.get(0);
			assertEquals(WhiteSpaceExtractor.ORIENTATION_LEFT, feature.getValue());
			feature = features.get(1);
			assertEquals(WhiteSpaceExtractor.ORIENTATION_RIGHT, feature.getValue());
			
		}}
