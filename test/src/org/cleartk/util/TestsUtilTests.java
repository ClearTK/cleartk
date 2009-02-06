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

import static org.junit.Assert.assertEquals;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.EmptyAnnotator;
import org.junit.Test;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

*/

public class TestsUtilTests {

	@Test
	public void testCreateTokens() throws ResourceInitializationException {
		AnalysisEngine engine = TestsUtil.getAnalysisEngine(
				EmptyAnnotator.class,
				TestsUtil.getTypeSystem("org.cleartk.TypeSystem"));
	
		JCas jCas = engine.newJCas();
		String text = "What if we built a rocket ship made of cheese?" +
					  "We could fly it to the moon for repairs.";
		TestsUtil.createTokens(jCas, text,
				"What if we built a rocket ship made of cheese ? \n We could fly it to the moon for repairs .",
				"A B C D E F G H I J K L M N O P Q R S T U", null);
		
		FSIndex sentenceIndex = jCas.getAnnotationIndex(Sentence.type);
		assertEquals(2, sentenceIndex.size());
		FSIterator sentences = sentenceIndex.iterator();
		Sentence sentence = (Sentence) sentences.next();
		assertEquals("What if we built a rocket ship made of cheese?", sentence.getCoveredText());
		sentence = (Sentence) sentences.next();
		assertEquals("We could fly it to the moon for repairs.", sentence.getCoveredText());
		
		FSIndex tokenIndex = jCas.getAnnotationIndex(Token.type);
		assertEquals(21, tokenIndex.size());
		Token token = AnnotationRetrieval.get(jCas, Token.class, 0);
		testToken(token, "What", 0, 4, "A", null);
		token = AnnotationRetrieval.get(jCas, Token.class, 1);
		testToken(token, "if", 5, 7, "B", null);
		token = AnnotationRetrieval.get(jCas, Token.class, 9);
		testToken(token, "cheese", 39, 45, "J", null);
		token = AnnotationRetrieval.get(jCas, Token.class, 10);
		testToken(token, "?", 45, 46, "K", null);
		token = AnnotationRetrieval.get(jCas, Token.class, 11);
		testToken(token, "We", 46, 48, "L", null);
		token = AnnotationRetrieval.get(jCas, Token.class, 12);
		testToken(token, "could", 49, 54, "M", null);
		token = AnnotationRetrieval.get(jCas, Token.class, 19);
		testToken(token, "repairs", 78, 85, "T", null);
		token = AnnotationRetrieval.get(jCas, Token.class, 20);
		testToken(token, ".", 85, 86, "U", null);

		jCas = engine.newJCas();
		text = "What if we built a rocket ship made of cheese? \n" +
					  "We could fly it to the moon for repairs.";
		TestsUtil.createTokens(jCas, text,
				"What if we built a rocket ship made of cheese ? \n We could fly it to the moon for repairs .",
				"A B C D E F G H I J K L M N O P Q R S T U", null);
		
		token = AnnotationRetrieval.get(jCas, Token.class, 10);
		testToken(token, "?", 45, 46, "K", null);
		token = AnnotationRetrieval.get(jCas, Token.class, 11);
		testToken(token, "We", 48, 50, "L", null);
		
		jCas = engine.newJCas();
		text = "What if we built a rocket ship made of cheese? \n" +
					  "We could fly it to the moon for repairs.";
		TestsUtil.createTokens(jCas, text,
				"What if we built a rocket ship made of cheese ?\nWe could fly it to the moon for repairs .",
				"A B C D E F G H I J K L M N O P Q R S T U", null);
		
		token = AnnotationRetrieval.get(jCas, Token.class, 10);
		testToken(token, "?", 45, 46, "K", null);
		token = AnnotationRetrieval.get(jCas, Token.class, 11);
		testToken(token, "We", 48, 50, "L", null);

		jCas = engine.newJCas();
		text = "If you like line writer, then you should really check out line rider.";
		TestsUtil.createTokens(jCas, text, null, null, null);

		tokenIndex = jCas.getAnnotationIndex(Token.type);
		assertEquals(13, tokenIndex.size());
		token = AnnotationRetrieval.get(jCas, Token.class, 0);
		testToken(token, "If", 0, 2, null, null);
		token = AnnotationRetrieval.get(jCas, Token.class, 12);
		testToken(token, "rider.", 63, 69, null, null);
		sentenceIndex = jCas.getAnnotationIndex(Sentence.type);
		assertEquals(1, sentenceIndex.size());
		sentence = AnnotationRetrieval.get(jCas, Sentence.class, 0);
		assertEquals(text, sentence.getCoveredText());

	}
	
	private void testToken(Token token, String coveredText, int begin, int end, String partOfSpeech, String stem) {
		assertEquals(coveredText, token.getCoveredText());
		assertEquals(begin, token.getBegin());
		assertEquals(end, token.getEnd());
		assertEquals(partOfSpeech, token.getPos());
		assertEquals(stem, token.getStem());
		
	}
	
	@Test
	public void testSpaceSplit() {
		String[] splits = " asdf ".split(" ");
		assertEquals(2, splits.length);
		
	}
	
	@Test
	public void testSetConfigurationParameters() throws ResourceInitializationException {
		AnalysisEngine engine = TestsUtil.getAnalysisEngine(
				EmptyAnnotator.class, TestsUtil.getTypeSystem("org.cleartk.TypeSystem"),
				"myBoolean", true,
				"myBooleans", new Boolean[] {true, false, true, false}, 
				"myFloat", 1.0f,
				"myFloats", new Float[] {2.0f, 2.1f, 3.0f},
				"myInt", 1,
				"myInts", new Integer[] {2,3,4},
				"myString", "yourString",
				"myStrings", new String[] {"yourString1", "yourString2", "yourString3"}
				);
		
		UimaContext context = engine.getUimaContext();
		assertEquals(true, context.getConfigParameterValue("myBoolean"));
		Boolean[] myBooleans = (Boolean[]) context.getConfigParameterValue("myBooleans"); 
		assertEquals(4, myBooleans.length);
		assertEquals(true, myBooleans[0]);
		assertEquals(false, myBooleans[1]);
		assertEquals(true, myBooleans[2]);
		assertEquals(false, myBooleans[3]);
		
		assertEquals(1.0f, context.getConfigParameterValue("myFloat"));
		Float[] myFloats = (Float[]) context.getConfigParameterValue("myFloats");
		assertEquals(3, myFloats.length);
		assertEquals(2.0d, myFloats[0].doubleValue(), 0.01d);
		assertEquals(2.1d, myFloats[1].doubleValue(), 0.01d);
		assertEquals(3.0d, myFloats[2].doubleValue(), 0.01d);
		
		assertEquals(1, context.getConfigParameterValue("myInt"));
		Integer[] myInts = (Integer[]) context.getConfigParameterValue("myInts");
		assertEquals(3, myInts.length);
		assertEquals(2l, myInts[0].longValue());
		assertEquals(3l, myInts[1].longValue());
		assertEquals(4l, myInts[2].longValue());

		assertEquals("yourString", context.getConfigParameterValue("myString"));
		String[] myStrings = (String[]) context.getConfigParameterValue("myStrings");
		assertEquals(3, myStrings.length);
		assertEquals("yourString1", myStrings[0]);
		assertEquals("yourString2", myStrings[1]);
		assertEquals("yourString3", myStrings[2]);

		}
	
		

	
}
