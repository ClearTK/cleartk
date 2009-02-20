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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;
import org.cleartk.type.Document;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.DocumentUtil;
import org.cleartk.util.EmptyAnnotator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.TokenFactory;
import org.uutuc.factory.TypeSystemDescriptionFactory;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * <p>
 * 
 * @author Steven Bethard
 */
public class BagExtractorTests {

	@Before
	public void setUp() throws Exception {
		this.engine = AnalysisEngineFactory.createAnalysisEngine(
				EmptyAnnotator.class,
				TypeSystemDescriptionFactory.createTypeSystemDescription(Document.class, Token.class, Sentence.class));;
		this.jCasObjects = new ArrayList<JCas>();
		this.expectedTokenLists = new ArrayList<List<String>>();
		this.expectedPOSLists = new ArrayList<List<String>>();

		this.add(
				"The man walked to the store.",
				"The man walked to the store .",
				"DT NN VBD IN DT NN .",
				"The man walked to the store .",
				"DT NN VBD IN .");

		this.add(
				"The big black bug bit the big black bear\n" +
				"and the big black bear bled blood",
				null,
				"DT JJ JJ NN VBD DT JJ JJ NN CC DT JJ JJ NN VBD NN",
				"The big black bug bit the bear and bled blood",
				"DT JJ NN VBD CC");
	}

	/**
	 * Tests a bag of Tokens where texts are extracted from each token.
	 * 
	 * @throws UIMAException
	 * @throws IOException
	 */
	@Test
	public void testSpannedText() throws UIMAException, IOException {
		SpannedTextExtractor textExtractor = new SpannedTextExtractor();
		BagExtractor bagExtractor = new BagExtractor(Token.class, textExtractor);
		this.testOne(bagExtractor, "BagOf_Token_SpannedText", this.expectedTokenLists);
	}

	/**
	 * Tests a bag of Tokens where texts are extracted from each token, and the
	 * extractor is given a name.
	 * 
	 * @throws UIMAException
	 * @throws IOException
	 */
	@Test
	public void testNamedSpannedText() throws UIMAException, IOException {
		SpannedTextExtractor textExtractor = new SpannedTextExtractor();
		BagExtractor bagExtractor = new BagExtractor("Name", Token.class, textExtractor);
		this.testOne(bagExtractor, "BagOf_Token_SpannedText_Name", this.expectedTokenLists);
	}

	/**
	 * Tests a bag of Tokens where part of speech tags are extracted from each
	 * token.
	 * 
	 * @throws UIMAException
	 * @throws IOException
	 */
	@Test
	public void testPartOfSpeech() throws UIMAException, IOException {
		TypePathExtractor posExtractor = new TypePathExtractor(Token.class, "pos");
		BagExtractor bagExtractor = new BagExtractor(Token.class, posExtractor);
		this.testOne(bagExtractor, "BagOf_Token_TypePath_Pos", this.expectedPOSLists);
	}

	/**
	 * Tests a bag of Tokens where part of speech tags are extracted from each
	 * token and the extractor is given a name.
	 * 
	 * @throws UIMAException
	 * @throws IOException
	 */
	@Test
	public void testNamedPartOfSpeech() throws UIMAException, IOException {
		TypePathExtractor posExtractor = new TypePathExtractor(Token.class, "pos");
		BagExtractor bagExtractor = new BagExtractor("Name", Token.class, posExtractor);
		this.testOne(bagExtractor, "BagOf_Token_TypePath_Pos_Name", this.expectedPOSLists);
	}

	private void add(
			String text,
			String tokensString,
			String posTagsString,
			String expectedTokensString,
			String expectedPOSString) throws UIMAException {
		
		// create the JCas and add the expected tokens and POS tags to the lists
		JCas jCas = this.engine.newJCas();
		this.jCasObjects.add(jCas);
		this.expectedTokenLists.add(Arrays.asList(expectedTokensString.split(" ")));
		this.expectedPOSLists.add(Arrays.asList(expectedPOSString.split(" ")));

		// set the document text and add Token annotations as indicated
		TokenFactory.createTokens(jCas, text, Token.class, Sentence.class, tokensString, posTagsString, null,
				"org.cleartk.type.Token:pos", null);
		DocumentUtil.createDocument(jCas, "foo", "bar");
	}

	private void testOne(
			BagExtractor bagExtractor,
			String nameString,
			List<List<String>> expectedValuesLists)
			throws UIMAException, IOException {

		// run a BagExtractor on each document
		for (int i = 0; i < this.jCasObjects.size(); i++) {
			JCas jCas = this.jCasObjects.get(i);
			Document document = DocumentUtil.getDocument(jCas);
			
			// collect all feature values, and check all feature names
			List<String> actualValues = new ArrayList<String>();
			for (Feature feature: bagExtractor.extract(jCas, document)) {
				actualValues.add(feature.getValue().toString());
				Assert.assertEquals(nameString, feature.getName());
			}

			// make sure the actual values match the expected ones
			Assert.assertEquals(expectedValuesLists.get(i), actualValues);
		}
	}
	
	private AnalysisEngine engine;
	private List<JCas> jCasObjects;
	private List<List<String>> expectedTokenLists;
	private List<List<String>> expectedPOSLists;
}
