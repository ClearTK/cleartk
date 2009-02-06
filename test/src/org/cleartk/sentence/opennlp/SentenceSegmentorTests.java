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
package org.cleartk.sentence.opennlp;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.cleartk.sentence.opennlp.OpenNLPSentenceSegmenter;
import org.cleartk.type.Sentence;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.TestsUtil;
import org.junit.Test;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * 
 * @author Philip Ogren, Steven Bethard
 */
public class SentenceSegmentorTests{
	
	@Test
	public void testManyNewlines() throws UIMAException {
		// create the SentenceSegmenter
		AnalysisEngine segmenter = TestsUtil.getAnalysisEngine(
				OpenNLPSentenceSegmenter.class,
				TestsUtil.getTypeSystem(Sentence.class),
				OpenNLPSentenceSegmenter.SENTENCE_MODEL_FILE_PARAM,
				"resources/models/OpenNLP.Sentence.English.bin.gz");

		// fill the JCas with newlines to provoke a StackOverflowError
		// when the regular expressions are bad
		JCas jCas = segmenter.newJCas();
		StringBuffer text = new StringBuffer();
		for (int i = 0; i < 1000; i++) {
			text.append('\n');
		}
		jCas.setDocumentText(text.toString());
		
		// run the sentence segmenter on the text
		segmenter.process(jCas);
		segmenter.collectionProcessComplete();
	}

	@Test
	public void testSentenceSegmentor() throws UIMAException, IOException
    {
		JCas jCas = TestsUtil.process(
				"org.cleartk.sentence.SentenceSegmenter",
				"test/data/docs/youthful-precocity.txt");

		Sentence sentence = AnnotationRetrieval.get(jCas, Sentence.class, 0);
		String sentenceText = "The precocity of some youths is surprising.";
		assertEquals(sentenceText, sentence.getCoveredText());
		
		sentence = AnnotationRetrieval.get(jCas, Sentence.class, 1);
		sentenceText = "One is disposed to say on occasion, \"That boy of yours is a genius, and he is certain to do great things when he grows up;\" but past experience has taught us that he invariably becomes quite an ordinary citizen.";
		assertEquals(sentenceText, sentence.getCoveredText());
		
		sentence = AnnotationRetrieval.get(jCas, Sentence.class, 2);
		sentenceText = "It is so often the case, on the contrary, that the dull boy becomes a great man.";
		assertEquals(sentenceText, sentence.getCoveredText());

		sentence = AnnotationRetrieval.get(jCas, Sentence.class, 3);
		sentenceText = "You never can tell.";
		assertEquals(sentenceText, sentence.getCoveredText());

		sentence = AnnotationRetrieval.get(jCas, Sentence.class, 4);
		sentenceText = "Nature loves to present to us these queer paradoxes.";
		assertEquals(sentenceText, sentence.getCoveredText());

		sentence = AnnotationRetrieval.get(jCas, Sentence.class, 5);
		sentenceText = "It is well known that those wonderful \"lightning calculators,\" who now and again surprise the world by their feats, lose all their mysterious powers directly they are taught the elementary rules of arithmetic.";
		assertEquals(sentenceText, sentence.getCoveredText());
		
    }
	
	@Test
	public void test1() throws UIMAException, IOException
    {
		JCas jCas = TestsUtil.process(
				"org.cleartk.sentence.SentenceSegmenter",
				"test/data/docs/sentences/test1.txt"); 
		
		Sentence sentence = AnnotationRetrieval.get(jCas, Sentence.class, 0);
		assertEquals("aaaa aaaa aaaa aaaa", sentence.getCoveredText());
		
		sentence = AnnotationRetrieval.get(jCas, Sentence.class, 1);
		assertEquals("bbbb", sentence.getCoveredText());
		
		sentence = AnnotationRetrieval.get(jCas, Sentence.class, 2);
		assertEquals("ccc cccc ccc cccc", sentence.getCoveredText());

		sentence = AnnotationRetrieval.get(jCas, Sentence.class, 3);
		assertEquals("dddddd ddd.", sentence.getCoveredText());
	}
	
	@Test
	public void test2() throws UIMAException, IOException
    {
		JCas jCas = TestsUtil.process(
				"org.cleartk.sentence.SentenceSegmenter",
				"test/data/docs/sentences/test2.txt");
		Sentence sentence = AnnotationRetrieval.get(jCas, Sentence.class, 0);
		assertEquals( "I don't understand this.", sentence.getCoveredText());
    }

	@Test
	public void test3() throws UIMAException, IOException
    {
		JCas jCas = TestsUtil.process(
				"org.cleartk.sentence.SentenceSegmenter",
				"test/data/docs/sentences/test3.txt");
		Sentence sentence = AnnotationRetrieval.get(jCas, Sentence.class, 0);
		assertEquals( "test", sentence.getCoveredText());
    }

	@Test
	public void test5() throws UIMAException, IOException{
		JCas jCas = TestsUtil.process(
				"org.cleartk.sentence.SentenceSegmenter",
				"test/data/docs/sentences/test5.txt");
		Sentence sentence = AnnotationRetrieval.get(jCas, Sentence.class, 0);
		assertEquals( "a", sentence.getCoveredText());
		sentence = AnnotationRetrieval.get(jCas, Sentence.class, 1);
		assertEquals( "b", sentence.getCoveredText());
		sentence = AnnotationRetrieval.get(jCas, Sentence.class, 2);
		assertEquals( "c", sentence.getCoveredText());
		sentence = AnnotationRetrieval.get(jCas, Sentence.class, 3);
		assertEquals( "d", sentence.getCoveredText());
		
	}

	@Test
	public void test6() throws UIMAException, IOException{
		JCas jCas = TestsUtil.process(
				"org.cleartk.sentence.SentenceSegmenter",
				"test/data/docs/sentences/test6.txt");
		Sentence sentence = AnnotationRetrieval.get(jCas, Sentence.class, 0);
		assertEquals( "a", sentence.getCoveredText());
		sentence = AnnotationRetrieval.get(jCas, Sentence.class, 1);
		assertEquals( "b", sentence.getCoveredText());
		sentence = AnnotationRetrieval.get(jCas, Sentence.class, 2);
		assertEquals( "c", sentence.getCoveredText());
		sentence = AnnotationRetrieval.get(jCas, Sentence.class, 3);
		assertEquals( "d", sentence.getCoveredText());
		
	}

	@Test
	public void test7() throws UIMAException, IOException{
		JCas jCas = TestsUtil.process(
				"org.cleartk.sentence.SentenceSegmenter",
				"test/data/docs/sentences/test7.txt");
		Sentence sentence = AnnotationRetrieval.get(jCas, Sentence.class, 0);
		assertEquals( "It was a Wednesday morning.", sentence.getCoveredText());
		
	}
}
