/* 
 * Copyright (c) 2010, Regents of the University of Colorado 
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

package org.cleartk.token.breakit;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.test.util.DefaultTestBase;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * 
 * @author Philip Ogren
 */

public class BreakIteratorAnnotatorTest extends DefaultTestBase {

  @Test
  public void testTokenAnnotator() throws Exception {
    AnalysisEngineDescription tokenAnnotator = BreakIteratorAnnotatorFactory.createTokenAnnotator(Locale.US);
    String text = "  : ;) Hey there!  I am going to the store.  Would you like to come with me?";
    String expectedText = ": ; ) Hey there ! I am going to the store . Would you like to come with me ?";
    test(tokenAnnotator, Token.class, text, expectedText);
  }

  @Test
  public void testSentenceAnnotator() throws Exception {
    AnalysisEngineDescription sentenceAnnotator = BreakIteratorAnnotatorFactory.createSentenceAnnotator(Locale.US);
    String text = "  : ;) Hey there!  I am going to the store.  Would you like to come with me?";
    String[] expectedAnnotations = new String[] {
        "  : ;) Hey there!  ",
        "I am going to the store.  ",
        "Would you like to come with me?" };
    test(sentenceAnnotator, Sentence.class, text, expectedAnnotations);
  }

  private void test(
      AnalysisEngineDescription annotator,
      Class<? extends Annotation> annotationCls,
      String text,
      String[] expectedAnnotations) throws Exception {
    jCas.setDocumentText(text);
    SimplePipeline.runPipeline(jCas, annotator);
    Collection<? extends Annotation> actualAnnotations = JCasUtil.select(jCas, annotationCls);
    assertEquals(expectedAnnotations.length, actualAnnotations.size());
    Iterator<? extends Annotation> iter = actualAnnotations.iterator();
    for (int i = 0; i < expectedAnnotations.length; i++) {
      assertEquals(expectedAnnotations[i], iter.next().getCoveredText());
    }
  }

  private void test(
      AnalysisEngineDescription annotator,
      Class<? extends Annotation> annotationCls,
      String text,
      String expectedText) throws Exception {
    String[] expectedAnnotations = expectedText.split(" ");
    test(annotator, annotationCls, text, expectedAnnotations);
  }

}
