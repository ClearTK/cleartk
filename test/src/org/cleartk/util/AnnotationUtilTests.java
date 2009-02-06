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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ne.type.NamedEntityMention;
import org.cleartk.type.Chunk;
import org.cleartk.type.ContiguousAnnotation;
import org.cleartk.type.Sentence;
import org.cleartk.type.SimpleAnnotation;
import org.cleartk.type.SplitAnnotation;
import org.cleartk.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.AnnotationUtil;
import org.cleartk.util.UIMAUtil;
import org.junit.Test;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * 
 * @author Philip Ogren
 */
public class AnnotationUtilTests {
	
	public static class Annotator extends JCasAnnotator_ImplBase
	{
		public static JCas getProcessedJCas() throws UIMAException, IOException {
			AnalysisEngine engine = TestsUtil.getAnalysisEngine(
					Annotator.class, TestsUtil.getTypeSystem("org.cleartk.TypeSystem"));
			return TestsUtil.process(engine, "test/data/docs/huckfinn.txt");
	
		}
	
		/**
		 * Assumes that we are processing the text test/data/docs/huckfinn.txt
		 * 
		 */
		@Override
		public void process(JCas jCas) throws AnalysisEngineProcessException
		{
			Token token0 = new Token(jCas, 0,4);
			token0.addToIndexes();
			
			NamedEntityMention nem0 = new NamedEntityMention(jCas, 0, 4);
			nem0.addToIndexes();
			
			NamedEntityMention nem1 = new NamedEntityMention(jCas, 0, 13);
			nem1.addToIndexes();
			
			
			Token token1 = new Token(jCas, 20, 25);
			token1.addToIndexes();
			Token token2 = new Token(jCas, 21, 25);
			token2.addToIndexes();
			Token token3 = new Token(jCas, 24, 25);
			token3.addToIndexes();
			Token token4 = new Token(jCas, 25, 28);
			token4.addToIndexes();
			Token token5 = new Token(jCas, 26, 30);
			token5.addToIndexes();
			
			NamedEntityMention nem2 = new NamedEntityMention(jCas, 19, 31);
			nem2.addToIndexes();
			NamedEntityMention nem3 = new NamedEntityMention(jCas, 20, 30);
			nem3.addToIndexes();
			NamedEntityMention nem4 = new NamedEntityMention(jCas, 21, 29);
			nem4.addToIndexes();
			NamedEntityMention nem5 = new NamedEntityMention(jCas, 21, 24);
			nem5.addToIndexes();
			NamedEntityMention nem6 = new NamedEntityMention(jCas, 24, 28);
			nem6.addToIndexes();
			NamedEntityMention nem7 = new NamedEntityMention(jCas, 25, 28);
			nem7.addToIndexes();
			
			
			NamedEntityMention nem8 = new NamedEntityMention(jCas, 49, 61);
			nem8.addToIndexes();
			NamedEntityMention nem9 = new NamedEntityMention(jCas, 50, 60);
			nem9.addToIndexes();
			NamedEntityMention nem10 = new NamedEntityMention(jCas, 50, 55);
			nem10.addToIndexes();
			NamedEntityMention nem11 = new NamedEntityMention(jCas, 55, 60);
			nem11.addToIndexes();
	
			
			//add some annotations that will allow us to test a split annotation
			Token token6 = new Token(jCas, 100, 104);
			token6.addToIndexes();
			Token token7 = new Token(jCas, 105, 109);
			token7.addToIndexes();
			Token token8 = new Token(jCas, 110, 114);
			token8.addToIndexes();
			Token token9 = new Token(jCas, 115, 119);
			token9.addToIndexes();
			Token token10 = new Token(jCas, 120, 124);
			token10.addToIndexes();
			
			Chunk chunk0 = new Chunk(jCas, 100, 109);
			chunk0.addToIndexes();
			Chunk chunk1 = new Chunk(jCas, 115, 124);
			chunk1.addToIndexes();
			
			SplitAnnotation split0 = new SplitAnnotation(jCas, 100, 124);
			split0.setAnnotations(UIMAUtil.toFSArray(jCas, Arrays.asList(new Chunk[] {chunk0, chunk1})));
			split0.addToIndexes();
			
			
			
		}
	
	}

	@Test
	public void testContains() throws UIMAException, IOException {
		JCas jCas = AnnotationUtilTests.Annotator.getProcessedJCas();
		
		Token token6 = AnnotationRetrieval.get(jCas, Token.class, 6);
		Token token7 = AnnotationRetrieval.get(jCas, Token.class, 7);
		Token token8 = AnnotationRetrieval.get(jCas, Token.class, 8);
		Token token9 = AnnotationRetrieval.get(jCas, Token.class, 9);
		Token token10 = AnnotationRetrieval.get(jCas, Token.class, 10);
		Chunk chunk0 = AnnotationRetrieval.get(jCas, Chunk.class, 0);
		Chunk chunk1 = AnnotationRetrieval.get(jCas, Chunk.class, 1);
		SplitAnnotation split0 = AnnotationRetrieval.get(jCas, SplitAnnotation.class, 0);

		assertTrue(AnnotationUtil.contains(token6, token6));
		assertTrue(AnnotationUtil.contains(chunk0, token6));
		assertTrue(AnnotationUtil.contains(chunk0, token7));
		assertFalse(AnnotationUtil.contains(chunk0, token8));
		assertTrue(AnnotationUtil.contains(chunk0, chunk0));
		assertFalse(AnnotationUtil.contains(chunk1, token8));
		assertTrue(AnnotationUtil.contains(chunk1, token9));
		assertTrue(AnnotationUtil.contains(chunk1, token10));
		
		assertTrue(AnnotationUtil.contains(split0, token6));
		assertTrue(AnnotationUtil.contains(split0, token7));
		assertFalse(AnnotationUtil.contains(split0, token8));
		assertTrue(AnnotationUtil.contains(split0, token9));
		assertTrue(AnnotationUtil.contains(split0, token10));
	}

	@Test
	public void testOverlaps() throws UIMAException, IOException {
		JCas jCas = TestsUtil.newJCas();
		
		Token token1 = new Token(jCas, 0,0);
		Token token2 = new Token(jCas, 0,0);
		assertTrue(AnnotationUtil.overlaps(token1, token2));
		assertTrue(AnnotationUtil.overlaps(token2, token1));

		token1 = new Token(jCas, 0,0);
		token2 = new Token(jCas, 0,1);
		assertTrue(AnnotationUtil.overlaps(token1, token2));
		assertTrue(AnnotationUtil.overlaps(token2, token1));

		token1 = new Token(jCas, 0,5);
		token2 = new Token(jCas, 5,10);
		assertFalse(AnnotationUtil.overlaps(token1, token2));
		assertFalse(AnnotationUtil.overlaps(token2, token1));

		token1 = new Token(jCas, 0,5);
		token2 = new Token(jCas, 4,10);
		assertTrue(AnnotationUtil.overlaps(token1, token2));
		assertTrue(AnnotationUtil.overlaps(token2, token1));

		token1 = new Token(jCas, 0,5);
		token2 = new Token(jCas, 6,10);
		assertFalse(AnnotationUtil.overlaps(token1, token2));
		assertFalse(AnnotationUtil.overlaps(token2, token1));

		token1 = new Token(jCas, 0,5);
		token2 = new Token(jCas, 1,10);
		assertTrue(AnnotationUtil.overlaps(token1, token2));
		assertTrue(AnnotationUtil.overlaps(token2, token1));

	}

	@Test
	public void testSort() throws UIMAException {
		JCas jCas = TestsUtil.newJCas();
		
		List<Annotation> annotations = new ArrayList<Annotation>();
		annotations.add(new Token(jCas, 19, 21));
		annotations.add(new Token(jCas, 0, 15));
		annotations.add(new Sentence(jCas, 0, 30));
		annotations.add(new Sentence(jCas, 16, 30));
		annotations.add(new ContiguousAnnotation(jCas, 1, 35));
		annotations.add(new ContiguousAnnotation(jCas, 1, 34));
		annotations.add(new ContiguousAnnotation(jCas, 1, 36));
		
		AnnotationUtil.sort(annotations);
		assertEquals(0, annotations.get(0).getBegin());
		assertEquals(15, annotations.get(0).getEnd());
		assertEquals(0, annotations.get(1).getBegin());
		assertEquals(30, annotations.get(1).getEnd());
		assertEquals(1, annotations.get(2).getBegin());
		assertEquals(34, annotations.get(2).getEnd());
		assertEquals(1, annotations.get(3).getBegin());
		assertEquals(35, annotations.get(3).getEnd());
		assertEquals(1, annotations.get(4).getBegin());
		assertEquals(36, annotations.get(4).getEnd());
		assertEquals(16, annotations.get(5).getBegin());
		assertEquals(30, annotations.get(5).getEnd());
		assertEquals(19, annotations.get(6).getBegin());
		assertEquals(21, annotations.get(6).getEnd());
		
	}
	
	@Test
	public void testGetSurroundingTexts() throws UIMAException {
		JCas jCas = TestsUtil.newJCas();
		TestsUtil.createTokens(jCas, "AAA BBB CCC DDDD EEEE FFFF", null, null, null);
		
		SimpleAnnotation sa = new SimpleAnnotation(jCas, 8, 11);
		sa.addToIndexes();
		assertEquals("CCC", sa.getCoveredText());
		assertEquals("BBB ", AnnotationUtil.getSurroundingText(jCas, sa, Token.class, 1, true));
		assertEquals("AAA BBB ", AnnotationUtil.getSurroundingText(jCas, sa, Token.class, 2, true));
		assertEquals("AAA BBB ", AnnotationUtil.getSurroundingText(jCas, sa, Token.class, 3, true));
		assertEquals("AAA BBB ", AnnotationUtil.getSurroundingText(jCas, sa, Token.class, 4, true));
		assertEquals(" DDDD", AnnotationUtil.getSurroundingText(jCas, sa, Token.class, 1, false));
		assertEquals(" DDDD EEEE", AnnotationUtil.getSurroundingText(jCas, sa, Token.class, 2, false));
		assertEquals(" DDDD EEEE FFFF", AnnotationUtil.getSurroundingText(jCas, sa, Token.class, 3, false));
		assertEquals(" DDDD EEEE FFFF", AnnotationUtil.getSurroundingText(jCas, sa, Token.class, 4, false));
		
		IllegalArgumentException iae = null;
		try {
			AnnotationUtil.getSurroundingText(jCas, sa, Token.class, 0, true);
		} catch(IllegalArgumentException e) {
			iae = e;
		}
		assertNotNull(iae);
		
		
	}
}
