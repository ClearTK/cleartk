/*
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

package org.cleartk.syntax.dependency.clear;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.cleartk.syntax.dependency.type.DependencyNode;
import org.cleartk.syntax.dependency.type.DependencyRelation;
import org.cleartk.syntax.dependency.type.TopDependencyNode;
import org.cleartk.test.CleartkTestBase;
import org.cleartk.token.lemma.choi.LemmaAnnotator;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.junit.Before;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.testing.factory.TokenBuilder;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * 
 * @author Philip Ogren
 */

public class ClearParserTest extends CleartkTestBase {

  @Override
  public String[] getTypeSystemDescriptorNames() {
    return new String[] {
        "org.cleartk.token.TypeSystem",
        "org.cleartk.syntax.dependency.TypeSystem" };
  }

  protected TokenBuilder<Token, Sentence> tokenBuilder;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    tokenBuilder = new TokenBuilder<Token, Sentence>(Token.class, Sentence.class, "pos", null);
  }

  @Test
  public void testClearParser() throws Exception {
    this.assumeBigMemoryTestsEnabled();
    this.logger.info(BIG_MEMORY_TEST_MESSAGE);

    AnalysisEngine lemmatizer = AnalysisEngineFactory.createPrimitive(
        LemmaAnnotator.class,
        typeSystemDescription);
    AnalysisEngine parser = AnalysisEngineFactory.createPrimitive(
        ClearParser.class,
        typeSystemDescription);

    tokenBuilder.buildTokens(jCas, "This is a test.", "This is a test .", "DT VB DT NN .");
    lemmatizer.process(jCas);
    parser.process(jCas);

    Sentence sentence = JCasUtil.selectByIndex(jCas, Sentence.class, 0);
    DependencyNode topNode = JCasUtil.selectCovered(jCas, DependencyNode.class, sentence).iterator().next();
    assertTrue(topNode instanceof TopDependencyNode);
    assertEquals("This is a test.", topNode.getCoveredText());
    DependencyRelation isRel = topNode.getChildRelations(0);
    testRelationChild(isRel, "is", "ROOT");
    DependencyRelation thisRel = isRel.getChild().getChildRelations(0);
    testRelationChild(thisRel, "This", "SBJ");
    DependencyRelation testRel = isRel.getChild().getChildRelations(1);
    testRelationChild(testRel, "test", "PRD");
    DependencyRelation dotRel = isRel.getChild().getChildRelations(2);
    testRelationChild(dotRel, ".", "P");
    DependencyRelation aRel = testRel.getChild().getChildRelations(0);
    testRelationChild(aRel, "a", "NMOD");

    jCas.reset();
    tokenBuilder.buildTokens(
        jCas,
        "The epicenter was 90 kilometres (55 miles) west-northwest of Chengdu, the capital of Sichuan, with a depth of 19 kilometres (12 mi).",
        "The epicenter was 90 kilometres ( 55 miles ) west-northwest of Chengdu , the capital of Sichuan , with a depth of 19 kilometres ( 12 mi ) .",
        "DT NN VBD VBN IN ( NN NNS ) NN IN NN , DT NN IN NN , IN DT NN IN CD NNS ( CD JJ ) .");
    lemmatizer.process(jCas);
    parser.process(jCas);

    sentence = JCasUtil.selectByIndex(jCas, Sentence.class, 0);
    topNode = JCasUtil.selectCovered(jCas, DependencyNode.class, sentence).iterator().next();
    assertTrue(topNode instanceof TopDependencyNode);
    assertEquals(
        "The epicenter was 90 kilometres (55 miles) west-northwest of Chengdu, the capital of Sichuan, with a depth of 19 kilometres (12 mi).",
        topNode.getCoveredText());
    DependencyRelation wasRel = topNode.getChildRelations(0);
    testRelationChild(wasRel, "was", "ROOT");
    testRelationChild(wasRel.getChild().getChildRelations(0), "epicenter", "SBJ");
    testRelationChild(
        wasRel.getChild().getChildRelations(0).getChild().getChildRelations(0),
        "The",
        "NMOD");
    testRelationChild(
        wasRel.getChild().getChildRelations(0).getChild().getChildRelations(1),
        "capital",
        "APPO");
    testRelationChild(wasRel.getChild().getChildRelations(1), "90", "VC");
    testRelationChild(
        wasRel.getChild().getChildRelations(1).getChild().getChildRelations(0),
        "kilometres",
        "ADV");
    testRelationChild(wasRel.getChild().getChildRelations(2), ")", "P");
    testRelationChild(
        wasRel.getChild().getChildRelations(2).getChild().getChildRelations(0),
        ",",
        "P");
    testRelationChild(
        wasRel.getChild().getChildRelations(2).getChild().getChildRelations(1),
        "(",
        "P");
    testRelationChild(
        wasRel.getChild().getChildRelations(2).getChild().getChildRelations(2),
        "12",
        "NMOD");
    testRelationChild(
        wasRel.getChild().getChildRelations(2).getChild().getChildRelations(3),
        "mi",
        "NMOD");
    testRelationChild(wasRel.getChild().getChildRelations(3), ".", "P");

  }

  private void testRelationChild(
      DependencyRelation rel,
      String expectedChildText,
      String expectedRelation) {
    assertEquals(expectedChildText, rel.getChild().getCoveredText());
    assertEquals(expectedRelation, rel.getRelation());
  }
}
