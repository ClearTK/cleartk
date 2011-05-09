/** 
 * Copyright (c) 2011, Regents of the University of Colorado 
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

package org.cleartk.util.ae.parenthetical;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Iterator;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.cleartk.type.test.Chunk;
import org.cleartk.type.test.NamedEntityMention;
import org.cleartk.type.test.Sentence;
import org.cleartk.util.UtilTestBase;
import org.cleartk.util.type.Parenthetical;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 */

public class ParentheticalAnnotatorTest extends UtilTestBase {

  @Test
  public void testDefault() throws Exception {
    AnalysisEngineDescription aed = ParentheticalAnnotator.getDescription(typeSystemDescription);
    testParens("(hello!)", aed, "(hello!)");
    testParens("((a) and b)", aed, "((a) and b)", "(a)");
    testParens(
        "from the following options ((a) and (b) and (c)).",
        aed,
        "((a) and (b) and (c))",
        "(a)",
        "(b)",
        "(c)");
    testParens(
        "Brain weights of the two high strains, BXD5 (540 9 mg SEM) and BALB / cJ (527 13 mg), are significantly greater (P < .001)",
        aed,
        "(540 9 mg SEM)",
        "(527 13 mg)",
        "(P < .001)");
    testParens(
        "(A (B C (D E)) F)(G H I)(J K (L M N) O)",
        aed,
        "(A (B C (D E)) F)",
        "(B C (D E))",
        "(D E)",
        "(G H I)",
        "(J K (L M N) O)",
        "(L M N)");
    testParens("(ASDF (FDSA)", aed, "(FDSA)");
    testParens("ASDF FDSA)", aed);
  }

  /**
   * In this test a sentence type has been specified but no sentences are in the cas so no
   * parentheticals are found.
   * 
   * @throws Exception
   */
  @Test
  public void testNoSentence() throws Exception {
    AnalysisEngineDescription aed = ParentheticalAnnotator.getDescription(
        typeSystemDescription,
        Sentence.class);
    testParens("(hello!)", aed);
  }

  @Test
  public void testSentence() throws Exception {
    AnalysisEngineDescription aed = ParentheticalAnnotator.getDescription(
        typeSystemDescription,
        Sentence.class);
    tokenBuilder.buildTokens(jCas, "(hello!)");
    testParens(aed, "(hello!)");

    jCas.reset();
    // the sentence break prevents the entire text from being annotated.
    tokenBuilder.buildTokens(jCas, "((he(llo!))(asdf)(asdf))", "((he(llo!))(asdf)\n(asdf))");
    testParens(aed, "(he(llo!))", "(llo!)", "(asdf)", "(asdf)");

    jCas.reset();
    // without the sentence break the entire text is also annotated.
    tokenBuilder.buildTokens(jCas, "((he(llo!))(asdf)(asdf))");
    testParens(aed, "((he(llo!))(asdf)(asdf))", "(he(llo!))", "(llo!)", "(asdf)", "(asdf)");

    jCas.reset();
    // here's a more typical case of why we might not want parenthesis matching across sentences.
    // if you don't reset parenthesis matching at each sentence, then
    // "(because they were fond of both. 2)" would get annotated.
    tokenBuilder.buildTokens(
        jCas,
        "1) They lauged and cried (because they were fond of both. 2) Then did neither.",
        "1) They lauged and cried (because they were fond of both.\n 2) Then did neither.");
    testParens(aed);

  }

  @Test
  public void testTypes() throws Exception {
    AnalysisEngineDescription aed = ParentheticalAnnotator.getDescription(
        typeSystemDescription,
        NamedEntityMention.class);
    ConfigurationParameterFactory.addConfigurationParameter(
        aed,
        ParentheticalAnnotator.PARAM_PARENTHETICAL_TYPE_NAME,
        Chunk.class.getName());
    AnalysisEngine ae = AnalysisEngineFactory.createPrimitive(aed);

    String text = "((a))(((abc)bc)def)";
    jCas.setDocumentText(text);
    new NamedEntityMention(jCas, 0, text.length()).addToIndexes();
    SimplePipeline.runPipeline(jCas, ae);
    Collection<Chunk> chunks = JCasUtil.select(jCas, Chunk.class);
    assertEquals(5, chunks.size());
    Iterator<Chunk> chunksIter = chunks.iterator();
    assertEquals("((a))", chunksIter.next().getCoveredText());
    assertEquals("(a)", chunksIter.next().getCoveredText());
    assertEquals("(((abc)bc)def)", chunksIter.next().getCoveredText());
    assertEquals("((abc)bc)", chunksIter.next().getCoveredText());
    assertEquals("(abc)", chunksIter.next().getCoveredText());

  }

  @Test
  public void testBrackets() throws Exception {
    AnalysisEngineDescription aed = AnalysisEngineFactory.createPrimitiveDescription(
        ParentheticalAnnotator.class,
        typeSystemDescription,
        ParentheticalAnnotator.PARAM_LEFT_PARENTHESIS,
        "[",
        ParentheticalAnnotator.PARAM_RIGHT_PARENTHESIS,
        "]");
    testParens("[[[a]b]ccc]", aed, "[[[a]b]ccc]", "[[a]b]", "[a]");

    AnalysisEngineDescription aed2 = ParentheticalAnnotator.getDescription(typeSystemDescription);
    AnalysisEngineDescription aed3 = AnalysisEngineFactory.createPrimitiveDescription(
        ParentheticalAnnotator.class,
        typeSystemDescription,
        ParentheticalAnnotator.PARAM_LEFT_PARENTHESIS,
        "{",
        ParentheticalAnnotator.PARAM_RIGHT_PARENTHESIS,
        "}");

    jCas.reset();
    jCas.setDocumentText("testBrackets(){ //[some code here]}");
    SimplePipeline.runPipeline(jCas, aed, aed2, aed3);
    Collection<Parenthetical> chunks = JCasUtil.select(jCas, Parenthetical.class);
    Iterator<Parenthetical> chunksIter = chunks.iterator();
    assertEquals(3, chunks.size());
    assertEquals("()", chunksIter.next().getCoveredText());
    assertEquals("{ //[some code here]}", chunksIter.next().getCoveredText());
    assertEquals("[some code here]", chunksIter.next().getCoveredText());

  }

  private void testParens(String text, AnalysisEngineDescription aed, String... parenStrings)
      throws Exception {
    jCas.reset();
    jCas.setDocumentText(text);
    testParens(aed, parenStrings);
  }

  private void testParens(AnalysisEngineDescription aed, String... parenStrings) throws Exception {
    AnalysisEngine ae = AnalysisEngineFactory.createPrimitive(aed);

    SimplePipeline.runPipeline(jCas, ae);

    // ae.process(jCas);
    Collection<Parenthetical> parens = JCasUtil.select(jCas, Parenthetical.class);
    assertEquals(parenStrings.length, parens.size());
    Iterator<Parenthetical> parensIter = parens.iterator();
    for (int i = 0; i < parenStrings.length; i++) {
      assertEquals(parenStrings[i], parensIter.next().getCoveredText());
    }
  }

}
