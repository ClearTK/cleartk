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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkComponents;
import org.cleartk.token.chunk.type.Subtoken;
import org.cleartk.token.util.PennTreebankTokenizer;
import org.cleartk.token.util.Subtokenizer;
import org.cleartk.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.TestsUtil;
import org.junit.Test;
import org.uutuc.factory.AnalysisEngineFactory;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 * 
 * @author Philip Ogren
 * @author Steven Bethard
 * 
 * 
 *         Many of the tests found in this file were translated directly from a
 *         script that Steven Bethard gave me called test_tokenizer.py.
 */

public class TokenizerAndTokenAnnotatorTests {

	private static AnalysisEngine sentencesAndTokens; 
	
	static {
	try {
		sentencesAndTokens = AnalysisEngineFactory.createAggregate(CleartkComponents.createSentencesAndTokens());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testMarysDog() throws UIMAException, IOException {
		JCas jCas = AnalysisEngineFactory.process(sentencesAndTokens, "test/data/docs/tokens/marysdog.txt");
		FSIndex tokenIndex = jCas.getAnnotationIndex(Token.type);
		assertEquals(37, tokenIndex.size());

		int index = 0;
		assertEquals("\"", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("John", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("&", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("Mary", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("'s", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("'", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("dog", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("'", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("...", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("\"", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals(",", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("Jane", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("thought", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("(", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("to", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("herself", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals(")", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals(".", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("\"", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("What", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("a", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("@", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("#", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("$", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("%", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("*", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("!", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("a", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("-", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("``", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("I", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("like", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("'", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("AT&T", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("'", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("''", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals(".", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
	}

	@Test
	public void testWatcha() throws UIMAException, IOException {
		JCas jCas = AnalysisEngineFactory.process(sentencesAndTokens, "test/data/docs/tokens/watcha.txt");
		FSIndex tokenIndex = jCas.getAnnotationIndex(Token.type);
		assertEquals(31, tokenIndex.size());

		int index = 0;
		assertEquals("I", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("ca", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("n't", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("believe", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("they", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("wan", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("na", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("keep", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("40", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("%", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("of", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("that", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals(".", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("\"", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("``", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("Wha", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("t", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("cha", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("think", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("?", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("''", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("\"", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("I", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("do", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("n't", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("---", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("think", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("so", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("...", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals(",", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("\"", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
	}

	@Test
	public void testTimes() throws UIMAException, IOException {
		JCas jCas = AnalysisEngineFactory.process(sentencesAndTokens, "test/data/docs/tokens/times.txt");
		FSIndex tokenIndex = jCas.getAnnotationIndex(Token.type);
		assertEquals(16, tokenIndex.size());

		int index = 0;
		assertEquals("I", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("said", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("at", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("4:45", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("pm", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals(".", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("I", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("was", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("born", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("in", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("'80", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals(",", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("not", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("the", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("'70s", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals(".", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
	}

	@Test
	public void testDollars() throws UIMAException, IOException {
		JCas jCas = AnalysisEngineFactory.process(sentencesAndTokens, "test/data/docs/tokens/dollars.txt");
		FSIndex tokenIndex = jCas.getAnnotationIndex(Token.type);
		assertEquals(16, tokenIndex.size());

		int index = 0;
		assertEquals("You", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("`", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("paid", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("'", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("US$", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("170,000", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("?", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("!", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("You", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("should", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("'ve", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("paid", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("only", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("$", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("16.75", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals(".", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
	}

	@Test
	public void testPercents() throws UIMAException, IOException {
		JCas jCas = AnalysisEngineFactory.process(sentencesAndTokens,
				" 1. Buy a new Chevrolet (37%-owned in the U.S..) . 15%");
		FSIndex tokenIndex = jCas.getAnnotationIndex(Token.type);
		assertEquals(16, tokenIndex.size());

		int index = 0;
		assertEquals("1", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals(".", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("Buy", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("a", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("new", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("Chevrolet", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("(", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("37%-owned", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("in", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("the", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("U.S.", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals(".", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals(")", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals(".", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("15", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
		assertEquals("%", AnnotationRetrieval.get(jCas, Token.class, index++).getCoveredText());
	}

	@Test
	public void testDescriptor() throws UIMAException, IOException {
		AnalysisEngine engine = CleartkComponents.createPrimitive(TokenAnnotator.class);
		assertEquals(PennTreebankTokenizer.class.getName(), engine.getConfigParameterValue(TokenAnnotator.PARAM_TOKENIZER));
		assertEquals(Token.class.getName(), engine.getConfigParameterValue(TokenAnnotator.PARAM_TOKEN_TYPE));
		engine.collectionProcessComplete();
	}

	@Test
	public void ticket176() throws ResourceInitializationException, AnalysisEngineProcessException {
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(TokenAnnotator.class,
				CleartkComponents.TYPE_SYSTEM_DESCRIPTION, TokenAnnotator.PARAM_TOKEN_TYPE, Subtoken.class.getName(),
				TokenAnnotator.PARAM_TOKENIZER, Subtokenizer.class.getName());
		JCas jCas = TestsUtil.getJCas();
		jCas.setDocumentText("AA;BB-CC   DD!@#$EE(FF)GGG \tH,.");
		engine.process(jCas);

		int index = 0;
		assertEquals("AA", AnnotationRetrieval.get(jCas, Subtoken.class, index++).getCoveredText());
		assertEquals(";", AnnotationRetrieval.get(jCas, Subtoken.class, index++).getCoveredText());
		assertEquals("BB", AnnotationRetrieval.get(jCas, Subtoken.class, index++).getCoveredText());
		assertEquals("-", AnnotationRetrieval.get(jCas, Subtoken.class, index++).getCoveredText());
		assertEquals("CC", AnnotationRetrieval.get(jCas, Subtoken.class, index++).getCoveredText());
		assertEquals("DD", AnnotationRetrieval.get(jCas, Subtoken.class, index++).getCoveredText());
		assertEquals("!", AnnotationRetrieval.get(jCas, Subtoken.class, index++).getCoveredText());
		assertEquals("@", AnnotationRetrieval.get(jCas, Subtoken.class, index++).getCoveredText());
		assertEquals("#", AnnotationRetrieval.get(jCas, Subtoken.class, index++).getCoveredText());
		assertEquals("$", AnnotationRetrieval.get(jCas, Subtoken.class, index++).getCoveredText());
		assertEquals("EE", AnnotationRetrieval.get(jCas, Subtoken.class, index++).getCoveredText());
		assertEquals("(", AnnotationRetrieval.get(jCas, Subtoken.class, index++).getCoveredText());
		assertEquals("FF", AnnotationRetrieval.get(jCas, Subtoken.class, index++).getCoveredText());
		assertEquals(")", AnnotationRetrieval.get(jCas, Subtoken.class, index++).getCoveredText());
		assertEquals("GGG", AnnotationRetrieval.get(jCas, Subtoken.class, index++).getCoveredText());
		assertEquals("H", AnnotationRetrieval.get(jCas, Subtoken.class, index++).getCoveredText());
		assertEquals(",", AnnotationRetrieval.get(jCas, Subtoken.class, index++).getCoveredText());
		assertEquals(".", AnnotationRetrieval.get(jCas, Subtoken.class, index++).getCoveredText());

	}

	/**
	 * This test ensures that the default tokenizer will handle periods right.
	 * This is important because if, for some reason, you do not run the default
	 * tokenizer (PennTreebankTokenizer) over sentences, then it does not handle
	 * tokenization correctly. This sentence was chosen because this is exactly
	 * where it failed when I took out the iterating over sentences in the
	 * TokenAnnotator.
	 * 
	 * @throws UIMAException
	 * @throws IOException
	 */
	@Test
	public void testPeriod() throws UIMAException, IOException {
		
		AnalysisEngine tokenAnnotator = AnalysisEngineFactory.createAggregate(CleartkComponents.createSentencesAndTokens());
		JCas jCas = AnalysisEngineFactory.process(tokenAnnotator,
				"The sides was so steep and the bushes so thick. We tramped and clumb. ");
		int i = 0;
		assertEquals("The", getToken(jCas, i++).getCoveredText());
		assertEquals("sides", getToken(jCas, i++).getCoveredText());
		assertEquals("was", getToken(jCas, i++).getCoveredText());
		assertEquals("so", getToken(jCas, i++).getCoveredText());
		assertEquals("steep", getToken(jCas, i++).getCoveredText());
		assertEquals("and", getToken(jCas, i++).getCoveredText());
		assertEquals("the", getToken(jCas, i++).getCoveredText());
		assertEquals("bushes", getToken(jCas, i++).getCoveredText());
		assertEquals("so", getToken(jCas, i++).getCoveredText());
		assertEquals("thick", getToken(jCas, i++).getCoveredText());
		assertEquals(".", getToken(jCas, i++).getCoveredText());
		assertEquals("We", getToken(jCas, i++).getCoveredText());
		assertEquals("tramped", getToken(jCas, i++).getCoveredText());
		assertEquals("and", getToken(jCas, i++).getCoveredText());
		assertEquals("clumb", getToken(jCas, i++).getCoveredText());
		assertEquals(".", getToken(jCas, i++).getCoveredText());

	}

	private Token getToken(JCas jCas, int i) {
		return AnnotationRetrieval.get(jCas, Token.class, i);
	}

}
