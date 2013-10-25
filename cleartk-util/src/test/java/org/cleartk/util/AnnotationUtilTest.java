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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.AnnotationFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.test.DefaultTestBase;
import org.cleartk.type.test.Chunk;
import org.cleartk.type.test.NamedEntityMention;
import org.cleartk.type.test.Sentence;
import org.cleartk.type.test.Token;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 */
public class AnnotationUtilTest extends DefaultTestBase {

  public static class Annotator extends JCasAnnotator_ImplBase {
    public static void getProcessedJCas(JCas jCas) throws Exception {
      AnalysisEngine engine = AnalysisEngineFactory.createEngine(Annotator.class);
      jCas.reset();
      jCas.setDocumentText(Files.toString(
          new File("src/test/resources/docs/huckfinn.txt"),
          Charsets.US_ASCII));
      engine.process(jCas);
      engine.collectionProcessComplete();
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {

      try {
        AnnotationFactory.createAnnotation(jCas, 0, 4, Token.class);
        AnnotationFactory.createAnnotation(jCas, 0, 4, NamedEntityMention.class);
        AnnotationFactory.createAnnotation(jCas, 0, 13, NamedEntityMention.class);
        AnnotationFactory.createAnnotation(jCas, 20, 25, Token.class);
        AnnotationFactory.createAnnotation(jCas, 21, 25, Token.class);
        AnnotationFactory.createAnnotation(jCas, 24, 25, Token.class);
        AnnotationFactory.createAnnotation(jCas, 25, 28, Token.class);
        AnnotationFactory.createAnnotation(jCas, 26, 30, Token.class);

        AnnotationFactory.createAnnotation(jCas, 19, 31, NamedEntityMention.class);
        AnnotationFactory.createAnnotation(jCas, 20, 30, NamedEntityMention.class);
        AnnotationFactory.createAnnotation(jCas, 21, 29, NamedEntityMention.class);
        AnnotationFactory.createAnnotation(jCas, 21, 24, NamedEntityMention.class);
        AnnotationFactory.createAnnotation(jCas, 24, 28, NamedEntityMention.class);
        AnnotationFactory.createAnnotation(jCas, 25, 28, NamedEntityMention.class);

        AnnotationFactory.createAnnotation(jCas, 49, 61, NamedEntityMention.class);
        AnnotationFactory.createAnnotation(jCas, 50, 60, NamedEntityMention.class);
        AnnotationFactory.createAnnotation(jCas, 50, 55, NamedEntityMention.class);
        AnnotationFactory.createAnnotation(jCas, 55, 60, NamedEntityMention.class);

        // add some annotations that will allow us to test a split annotation
        AnnotationFactory.createAnnotation(jCas, 100, 104, Token.class);
        AnnotationFactory.createAnnotation(jCas, 105, 109, Token.class);
        AnnotationFactory.createAnnotation(jCas, 110, 114, Token.class);
        AnnotationFactory.createAnnotation(jCas, 115, 119, Token.class);
        AnnotationFactory.createAnnotation(jCas, 120, 124, Token.class);

        AnnotationFactory.createAnnotation(jCas, 100, 109, Chunk.class);
        AnnotationFactory.createAnnotation(jCas, 115, 124, Chunk.class);

      } catch (UIMAException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

  }

  @Test
  public void testContains() throws Exception {
    AnnotationUtilTest.Annotator.getProcessedJCas(jCas);

    Token token6 = JCasUtil.selectByIndex(jCas, Token.class, 6);
    Token token7 = JCasUtil.selectByIndex(jCas, Token.class, 7);
    Token token8 = JCasUtil.selectByIndex(jCas, Token.class, 8);
    Token token9 = JCasUtil.selectByIndex(jCas, Token.class, 9);
    Token token10 = JCasUtil.selectByIndex(jCas, Token.class, 10);
    Chunk chunk0 = JCasUtil.selectByIndex(jCas, Chunk.class, 0);
    Chunk chunk1 = JCasUtil.selectByIndex(jCas, Chunk.class, 1);

    assertTrue(AnnotationUtil.contains(token6, token6));
    assertTrue(AnnotationUtil.contains(chunk0, token6));
    assertTrue(AnnotationUtil.contains(chunk0, token7));
    assertFalse(AnnotationUtil.contains(chunk0, token8));
    assertTrue(AnnotationUtil.contains(chunk0, chunk0));
    assertFalse(AnnotationUtil.contains(chunk1, token8));
    assertTrue(AnnotationUtil.contains(chunk1, token9));
    assertTrue(AnnotationUtil.contains(chunk1, token10));

  }

  @Test
  public void testOverlaps() {
    Token token1 = new Token(jCas, 0, 0);
    Token token2 = new Token(jCas, 0, 0);
    assertTrue(AnnotationUtil.overlaps(token1, token2));
    assertTrue(AnnotationUtil.overlaps(token2, token1));

    token1 = new Token(jCas, 0, 0);
    token2 = new Token(jCas, 0, 1);
    assertTrue(AnnotationUtil.overlaps(token1, token2));
    assertTrue(AnnotationUtil.overlaps(token2, token1));

    token1 = new Token(jCas, 0, 5);
    token2 = new Token(jCas, 5, 10);
    assertFalse(AnnotationUtil.overlaps(token1, token2));
    assertFalse(AnnotationUtil.overlaps(token2, token1));

    token1 = new Token(jCas, 0, 5);
    token2 = new Token(jCas, 4, 10);
    assertTrue(AnnotationUtil.overlaps(token1, token2));
    assertTrue(AnnotationUtil.overlaps(token2, token1));

    token1 = new Token(jCas, 0, 5);
    token2 = new Token(jCas, 6, 10);
    assertFalse(AnnotationUtil.overlaps(token1, token2));
    assertFalse(AnnotationUtil.overlaps(token2, token1));

    token1 = new Token(jCas, 0, 5);
    token2 = new Token(jCas, 1, 10);
    assertTrue(AnnotationUtil.overlaps(token1, token2));
    assertTrue(AnnotationUtil.overlaps(token2, token1));

  }

  @Test
  public void testSort() {
    List<Annotation> annotations = new ArrayList<Annotation>();
    annotations.add(new Token(jCas, 19, 21));
    annotations.add(new Token(jCas, 0, 15));
    annotations.add(new Sentence(jCas, 0, 30));
    annotations.add(new Sentence(jCas, 16, 30));
    annotations.add(new Annotation(jCas, 1, 35));
    annotations.add(new Annotation(jCas, 1, 34));
    annotations.add(new Annotation(jCas, 1, 36));

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
    tokenBuilder.buildTokens(jCas, "AAA BBB CCC DDDD EEEE FFFF");

    Annotation sa = new Annotation(jCas, 8, 11);
    sa.addToIndexes();
    assertEquals("CCC", sa.getCoveredText());
    assertEquals("BBB ", AnnotationUtil.getSurroundingText(jCas, sa, Token.class, 1, true));
    assertEquals("AAA BBB ", AnnotationUtil.getSurroundingText(jCas, sa, Token.class, 2, true));
    assertEquals("AAA BBB ", AnnotationUtil.getSurroundingText(jCas, sa, Token.class, 3, true));
    assertEquals("AAA BBB ", AnnotationUtil.getSurroundingText(jCas, sa, Token.class, 4, true));
    assertEquals(" DDDD", AnnotationUtil.getSurroundingText(jCas, sa, Token.class, 1, false));
    assertEquals(" DDDD EEEE", AnnotationUtil.getSurroundingText(jCas, sa, Token.class, 2, false));
    assertEquals(
        " DDDD EEEE FFFF",
        AnnotationUtil.getSurroundingText(jCas, sa, Token.class, 3, false));
    assertEquals(
        " DDDD EEEE FFFF",
        AnnotationUtil.getSurroundingText(jCas, sa, Token.class, 4, false));

    IllegalArgumentException iae = null;
    try {
      AnnotationUtil.getSurroundingText(jCas, sa, Token.class, 0, true);
    } catch (IllegalArgumentException e) {
      iae = e;
    }
    assertNotNull(iae);

  }
}
