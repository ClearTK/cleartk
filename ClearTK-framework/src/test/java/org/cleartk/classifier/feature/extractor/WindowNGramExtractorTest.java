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

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.jcas.JCas;
import org.cleartk.CleartkException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.WindowNGramFeature;
import org.cleartk.classifier.feature.extractor.simple.SpannedTextExtractor;
import org.cleartk.type.test.Sentence;
import org.cleartk.type.test.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.JCasUtil;
import org.junit.Test;
import org.uimafit.testing.factory.TokenFactory;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 */

public class WindowNGramExtractorTest {

	@Test
	public void testLeftGrams() throws IOException, UIMAException, CleartkException {
		WindowNGramExtractor extractor = new WindowNGramExtractor(
				Token.class, new SpannedTextExtractor(),
				WindowNGramFeature.ORIENTATION_LEFT,
				WindowNGramFeature.DIRECTION_LEFT_TO_RIGHT, " ", 0, 3);

		// feature extraction on "island" in "...middle of the island..."
		JCas jCas = JCasUtil.getJCas();
		TokenFactory.createTokens(jCas,
				"text obtained from gutenberg\n" +
				"I WANTED to go and look at a place right about the middle of " +
				"the island that I ' d found when I was exploring ;\n" +
				"so we started and soon got to it", Token.class, Sentence.class);
		Token token = AnnotationRetrieval.get(jCas, Token.class, 19);
		assertEquals("island", token.getCoveredText());
		Feature feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("WindowNGram_L0_3gram_L2R", feature.getName());
		assertEquals("of the island", feature.getValue().toString());

		// "I" - the first word of the sentence
		token = AnnotationRetrieval.get(jCas, Token.class, 4);
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("OOB2 OOB1 I", feature.getValue().toString());
		// "WANTED"
		token = AnnotationRetrieval.get(jCas, Token.class, 5);
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("OOB1 I WANTED", feature.getValue().toString());
		// "to"
		token = AnnotationRetrieval.get(jCas, Token.class, 6);
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("I WANTED to", feature.getValue().toString());
		// "go"
		token = AnnotationRetrieval.get(jCas, Token.class, 7);
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("WANTED to go", feature.getValue().toString());
		// "exploring"
		token = AnnotationRetrieval.get(jCas, Token.class, 28);
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("I was exploring", feature.getValue().toString());
		// ";"
		token = AnnotationRetrieval.get(jCas, Token.class, 29);
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("was exploring ;", feature.getValue().toString());

		// behavior when there is no encapsulating sentence
		jCas.reset();
		jCas.setDocumentText("foo bar baz");
		token = new Token(jCas, 4, 7);
		token.addToIndexes();
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("OOB3 OOB2 OOB1", feature.getValue().toString());

		extractor = new WindowNGramExtractor(Token.class,
				new SpannedTextExtractor(),
				WindowNGramFeature.ORIENTATION_LEFT,
				WindowNGramFeature.DIRECTION_RIGHT_TO_LEFT, " ", 0, 3);

		jCas.reset();
		TokenFactory.createTokens(jCas,
				"text obtained from gutenberg\n" +
				"I WANTED to go and look at a place right about the middle of " +
				"the island that I ' d found when I was exploring ;\n" +
				"so we started and soon got to it", Token.class, Sentence.class);
		token = AnnotationRetrieval.get(jCas, Token.class, 19);
		assertEquals("island", token.getCoveredText());
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("WindowNGram_L0_3gram_R2L", feature.getName());
		assertEquals("island the of", feature.getValue().toString());

		token = AnnotationRetrieval.get(jCas, Token.class, 4);
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("I OOB1 OOB2", feature.getValue().toString());

		token = AnnotationRetrieval.get(jCas, Token.class, 5);
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("WANTED I OOB1", feature.getValue().toString());

		token = AnnotationRetrieval.get(jCas, Token.class, 6);
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("to WANTED I", feature.getValue().toString());

		token = AnnotationRetrieval.get(jCas, Token.class, 7);
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("go to WANTED", feature.getValue().toString());

		// behavior when there is no encapsulating sentence
		jCas.reset();
		jCas.setDocumentText("foo bar baz");
		token = new Token(jCas, 4, 7);
		token.addToIndexes();
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals(feature.getValue().toString(), "OOB1 OOB2 OOB3");

		extractor = new WindowNGramExtractor(Token.class,
				new SpannedTextExtractor(),
				WindowNGramFeature.ORIENTATION_LEFT,
				WindowNGramFeature.DIRECTION_LEFT_TO_RIGHT, " ", 2, 4);

		jCas.reset();
		TokenFactory.createTokens(jCas,
				"text obtained from gutenberg\n" +
				"I WANTED to go and look at a place right about the middle of " +
				"the island that I ' d found when I was exploring ;\n" +
				"so we started and soon got to it", Token.class, Sentence.class);
		token = AnnotationRetrieval.get(jCas, Token.class, 19);
		assertEquals("island", token.getCoveredText());
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("WindowNGram_L2_2gram_L2R", feature.getName());
		assertEquals("middle of", feature.getValue().toString());

		token = AnnotationRetrieval.get(jCas, Token.class, 4);
		assertEquals("I", token.getCoveredText());
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("OOB3 OOB2", feature.getValue().toString());

		token = AnnotationRetrieval.get(jCas, Token.class, 5);
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("OOB2 OOB1", feature.getValue().toString());

		token = AnnotationRetrieval.get(jCas, Token.class, 6);
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("OOB1 I", feature.getValue().toString());

		token = AnnotationRetrieval.get(jCas, Token.class, 7);
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("I WANTED", feature.getValue().toString());

		token = AnnotationRetrieval.get(jCas, Token.class, 8);
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("WANTED to", feature.getValue().toString());

		// behavior when there is no encapsulating sentence
		jCas.reset();
		jCas.setDocumentText("foo bar baz");
		token = new Token(jCas, 4, 7);
		token.addToIndexes();
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("OOB4 OOB3", feature.getValue().toString());
	}

	@Test
	public void testRightGrams() throws IOException, UIMAException, CleartkException {
		WindowNGramExtractor extractor = new WindowNGramExtractor(
				Token.class, new SpannedTextExtractor(),
				WindowNGramFeature.ORIENTATION_RIGHT,
				WindowNGramFeature.DIRECTION_LEFT_TO_RIGHT, " ", 0, 3);

		// feature extraction on "island" in "...because the island was only..."
		JCas jCas = JCasUtil.getJCas();
		TokenFactory.createTokens(jCas,
				"text obtained from gutenberg\n" +
				"I WANTED to go and look at a place right about the middle of " +
				"the island that I ' d found when I was exploring ;\n" +
				"so we started and soon got to it", Token.class, Sentence.class);
		Token token = AnnotationRetrieval.get(jCas, Token.class, 19);
		assertEquals("island", token.getCoveredText());
		Feature feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("WindowNGram_R0_3gram_L2R", feature.getName());
		assertEquals("island that I", feature.getValue().toString());

		// "I" - the first word of the sentence
		token = AnnotationRetrieval.get(jCas, Token.class, 4);
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("I WANTED to", feature.getValue().toString());
		// "WANTED"
		token = AnnotationRetrieval.get(jCas, Token.class, 5);
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("WANTED to go", feature.getValue().toString());

		token = AnnotationRetrieval.get(jCas, Token.class, 29);
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("; OOB1 OOB2", feature.getValue().toString());

		token = AnnotationRetrieval.get(jCas, Token.class, 28);
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("exploring ; OOB1", feature.getValue().toString());

		token = AnnotationRetrieval.get(jCas, Token.class, 27);
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("was exploring ;", feature.getValue().toString());

		token = AnnotationRetrieval.get(jCas, Token.class, 26);
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("I was exploring", feature.getValue().toString());

		// behavior when there is no encapsulating sentence
		jCas.reset();
		jCas.setDocumentText("foo bar baz");
		token = new Token(jCas, 4, 7);
		token.addToIndexes();
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("OOB1 OOB2 OOB3", feature.getValue().toString());

		extractor = new WindowNGramExtractor(Token.class,
				new SpannedTextExtractor(),
				WindowNGramFeature.ORIENTATION_RIGHT,
				WindowNGramFeature.DIRECTION_RIGHT_TO_LEFT, " ", 2, 8);

		// feature extraction on "island" in "...because the island was only..."
		jCas.reset();
		TokenFactory.createTokens(jCas,
				"text obtained from gutenberg\n" +
				"I WANTED to go and look at a place right about the middle of " +
				"the island that I ' d found when I was exploring ;\n" +
				"so we started and soon got to it", Token.class, Sentence.class);
		token = AnnotationRetrieval.get(jCas, Token.class, 19);
		assertEquals("island", token.getCoveredText());
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("WindowNGram_R2_6gram_R2L", feature.getName());
		assertEquals("I when found d ' I", feature.getValue().toString());

		// "I" - the first word of the sentence
		token = AnnotationRetrieval.get(jCas, Token.class, 4);
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("a at look and go to", feature.getValue().toString());
		// "WANTED"
		token = AnnotationRetrieval.get(jCas, Token.class, 5);
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("place a at look and go", feature.getValue().toString());

		token = AnnotationRetrieval.get(jCas, Token.class, 29);
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("OOB7 OOB6 OOB5 OOB4 OOB3 OOB2", feature.getValue()
				.toString());

		token = AnnotationRetrieval.get(jCas, Token.class, 28);
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("OOB6 OOB5 OOB4 OOB3 OOB2 OOB1", feature.getValue()
				.toString());

		token = AnnotationRetrieval.get(jCas, Token.class, 27);
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("OOB5 OOB4 OOB3 OOB2 OOB1 ;", feature.getValue()
				.toString());

		token = AnnotationRetrieval.get(jCas, Token.class, 26);
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals("OOB4 OOB3 OOB2 OOB1 ; exploring", feature.getValue()
				.toString());

		// behavior when there is no encapsulating sentence
		jCas.reset();
		jCas.setDocumentText("foo bar baz");
		token = new Token(jCas, 4, 7);
		token.addToIndexes();
		feature = extractor.extract(jCas, token, Sentence.class);
		assertEquals(feature.getValue().toString(),
				"OOB8 OOB7 OOB6 OOB5 OOB4 OOB3");

	}
}
