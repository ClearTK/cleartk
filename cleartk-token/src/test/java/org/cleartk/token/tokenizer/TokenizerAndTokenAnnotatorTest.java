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
package org.cleartk.token.tokenizer;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.token.TokenComponents;
import org.cleartk.token.TokenTestBase;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Subtoken;
import org.cleartk.token.type.Token;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.util.JCasUtil;

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
 *         Many of the tests found in this file were translated directly from a script that Steven
 *         Bethard gave me called test_tokenizer.py.
 */

public class TokenizerAndTokenAnnotatorTest extends TokenTestBase {

  private static AnalysisEngineDescription tokenizer;

  static {
    try {
      tokenizer = TokenComponents.createPennTokenizer();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testMarysDog() throws UIMAException, IOException {
    String text = FileUtils.readFileToString(new File("src/test/resources/token/marysdog.txt"));
    jCas.setDocumentText(text);
    new Sentence(jCas, 0, 52).addToIndexes();
    new Sentence(jCas, 54, 68).addToIndexes();
    new Sentence(jCas, 70, 91).addToIndexes();
    SimplePipeline.runPipeline(jCas, tokenizer);
    FSIndex<Annotation> tokenIndex = jCas.getAnnotationIndex(Token.type);
    assertEquals(37, tokenIndex.size());

    int index = 0;
    assertEquals("\"", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("John", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("&", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("Mary", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("'s", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("'", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("dog", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("'", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("...", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("\"", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals(",", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("Jane", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("thought", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("(", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("to", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("herself", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals(")", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals(".", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("\"", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("What", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("a", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("@", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("#", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("$", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("%", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("*", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("!", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("a", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("-", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("``", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("I", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("like", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("'", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("AT&T", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("'", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("''", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals(".", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
  }

  @Test
  public void testWatcha() throws UIMAException, IOException {
    String text = FileUtils.readFileToString(new File("src/test/resources/token/watcha.txt"));
    jCas.setDocumentText(text);
    new Sentence(jCas, 0, 45).addToIndexes();
    new Sentence(jCas, 47, 73).addToIndexes();
    new Sentence(jCas, 75, 109).addToIndexes();
    SimplePipeline.runPipeline(jCas, tokenizer);
    FSIndex<Annotation> tokenIndex = jCas.getAnnotationIndex(Token.type);
    assertEquals(31, tokenIndex.size());

    int index = 0;
    assertEquals("I", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("ca", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("n't", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("believe", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("they", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("wan", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("na", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("keep", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("40", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("%", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("of", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("that", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals(".", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("\"", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("``", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("Wha", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("t", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("cha", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("think", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("?", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("''", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("\"", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("I", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("do", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("n't", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("---", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("think", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("so", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("...", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals(",", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("\"", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
  }

  @Test
  public void testTimes() throws UIMAException, IOException {
    String text = FileUtils.readFileToString(new File("src/test/resources/token/times.txt"));
    jCas.setDocumentText(text);
    new Sentence(jCas, 0, 17).addToIndexes();
    new Sentence(jCas, 19, 59).addToIndexes();
    SimplePipeline.runPipeline(jCas, tokenizer);

    FSIndex<Annotation> tokenIndex = jCas.getAnnotationIndex(Token.type);
    assertEquals(16, tokenIndex.size());

    int index = 0;
    assertEquals("I", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("said", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("at", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("4:45", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("pm", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals(".", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("I", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("was", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("born", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("in", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("'80", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals(",", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("not", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("the", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("'70s", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals(".", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
  }

  @Test
  public void testDollars() throws UIMAException, IOException {
    String text = FileUtils.readFileToString(new File("src/test/resources/token/dollars.txt"));
    jCas.setDocumentText(text);
    new Sentence(jCas, 9, 33).addToIndexes();
    new Sentence(jCas, 34, 73).addToIndexes();
    SimplePipeline.runPipeline(jCas, tokenizer);
    FSIndex<Annotation> tokenIndex = jCas.getAnnotationIndex(Token.type);
    assertEquals(16, tokenIndex.size());

    int index = 0;
    assertEquals("You", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("`", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("paid", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("'", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("US$", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("170,000", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("?", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("!", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("You", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("should", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("'ve", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("paid", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("only", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("$", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("16.75", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals(".", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
  }

  @Test
  public void testPercents() throws UIMAException, IOException {

    jCas.setDocumentText(" 1. Buy a new Chevrolet (37%-owned in the U.S..) . 15%");
    new Sentence(jCas, 0, 54).addToIndexes();
    SimplePipeline.runPipeline(jCas, tokenizer);
    FSIndex<Annotation> tokenIndex = jCas.getAnnotationIndex(Token.type);
    assertEquals(16, tokenIndex.size());

    int index = 0;
    assertEquals("1", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals(".", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("Buy", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("a", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("new", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("Chevrolet", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("(", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("37%-owned", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("in", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("the", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("U.S.", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals(".", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals(")", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals(".", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("15", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
    assertEquals("%", JCasUtil.selectByIndex(jCas, Token.class, index++).getCoveredText());
  }

  @Test
  public void testDescriptor() throws UIMAException {
    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(TokenAnnotator.class);
    assertEquals(
        PennTreebankTokenizer.class.getName(),
        engine.getConfigParameterValue(TokenAnnotator.PARAM_TOKENIZER_NAME));
    assertEquals(
        Token.class.getName(),
        engine.getConfigParameterValue(TokenAnnotator.PARAM_TOKEN_TYPE_NAME));
    engine.collectionProcessComplete();
  }

  @Test
  public void ticket176() throws ResourceInitializationException, AnalysisEngineProcessException {
    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
        TokenAnnotator.class,
        TokenAnnotator.PARAM_TOKEN_TYPE_NAME,
        Subtoken.class.getName(),
        TokenAnnotator.PARAM_TOKENIZER_NAME,
        Subtokenizer.class.getName(),
        TokenAnnotator.PARAM_WINDOW_TYPE_NAME,
        DocumentAnnotation.class.getName());
    jCas.setDocumentText("AA;BB-CC   DD!@#$EE(FF)GGG \tH,.");
    engine.process(jCas);

    int index = 0;
    assertEquals("AA", JCasUtil.selectByIndex(jCas, Subtoken.class, index++).getCoveredText());
    assertEquals(";", JCasUtil.selectByIndex(jCas, Subtoken.class, index++).getCoveredText());
    assertEquals("BB", JCasUtil.selectByIndex(jCas, Subtoken.class, index++).getCoveredText());
    assertEquals("-", JCasUtil.selectByIndex(jCas, Subtoken.class, index++).getCoveredText());
    assertEquals("CC", JCasUtil.selectByIndex(jCas, Subtoken.class, index++).getCoveredText());
    assertEquals("DD", JCasUtil.selectByIndex(jCas, Subtoken.class, index++).getCoveredText());
    assertEquals("!", JCasUtil.selectByIndex(jCas, Subtoken.class, index++).getCoveredText());
    assertEquals("@", JCasUtil.selectByIndex(jCas, Subtoken.class, index++).getCoveredText());
    assertEquals("#", JCasUtil.selectByIndex(jCas, Subtoken.class, index++).getCoveredText());
    assertEquals("$", JCasUtil.selectByIndex(jCas, Subtoken.class, index++).getCoveredText());
    assertEquals("EE", JCasUtil.selectByIndex(jCas, Subtoken.class, index++).getCoveredText());
    assertEquals("(", JCasUtil.selectByIndex(jCas, Subtoken.class, index++).getCoveredText());
    assertEquals("FF", JCasUtil.selectByIndex(jCas, Subtoken.class, index++).getCoveredText());
    assertEquals(")", JCasUtil.selectByIndex(jCas, Subtoken.class, index++).getCoveredText());
    assertEquals("GGG", JCasUtil.selectByIndex(jCas, Subtoken.class, index++).getCoveredText());
    assertEquals("H", JCasUtil.selectByIndex(jCas, Subtoken.class, index++).getCoveredText());
    assertEquals(",", JCasUtil.selectByIndex(jCas, Subtoken.class, index++).getCoveredText());
    assertEquals(".", JCasUtil.selectByIndex(jCas, Subtoken.class, index++).getCoveredText());

  }

  /**
   * This test ensures that the default tokenizer will handle periods right. This is important
   * because if, for some reason, you do not run the default tokenizer (PennTreebankTokenizer) over
   * sentences, then it does not handle tokenization correctly. This sentence was chosen because
   * this is exactly where it failed when I took out the iterating over sentences in the
   * TokenAnnotator.
   * 
   * @throws UIMAException
   * @throws IOException
   */
  @Test
  public void testPeriod() throws UIMAException, IOException {
    String text = "The sides was so steep and the bushes so thick. We tramped and clumb. ";
    jCas.setDocumentText(text);
    new Sentence(jCas, 0, 47).addToIndexes();
    new Sentence(jCas, 48, 70).addToIndexes();
    SimplePipeline.runPipeline(jCas, tokenizer);
    int i = 0;
    assertEquals("The", getToken(i++).getCoveredText());
    assertEquals("sides", getToken(i++).getCoveredText());
    assertEquals("was", getToken(i++).getCoveredText());
    assertEquals("so", getToken(i++).getCoveredText());
    assertEquals("steep", getToken(i++).getCoveredText());
    assertEquals("and", getToken(i++).getCoveredText());
    assertEquals("the", getToken(i++).getCoveredText());
    assertEquals("bushes", getToken(i++).getCoveredText());
    assertEquals("so", getToken(i++).getCoveredText());
    assertEquals("thick", getToken(i++).getCoveredText());
    assertEquals(".", getToken(i++).getCoveredText());
    assertEquals("We", getToken(i++).getCoveredText());
    assertEquals("tramped", getToken(i++).getCoveredText());
    assertEquals("and", getToken(i++).getCoveredText());
    assertEquals("clumb", getToken(i++).getCoveredText());
    assertEquals(".", getToken(i++).getCoveredText());

  }

  private Token getToken(int i) {
    return JCasUtil.selectByIndex(jCas, Token.class, i);
  }

}
