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

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.cleartk.syntax.dependency.type.DependencyNode;
import org.cleartk.test.CleartkTestBase;
import org.cleartk.token.lemma.choi.LemmaAnnotator;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.cleartk.util.AnnotationRetrieval;
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
    DependencyNode topNode = AnnotationRetrieval.getContainingAnnotation(
        jCas,
        sentence,
        DependencyNode.class);
    testNode(topNode, "This is a test.", "#$ROOT$#");
    DependencyNode isNode = topNode.getChildren(0);
    testNode(isNode, "is", "ROOT");
    DependencyNode thisNode = isNode.getChildren(0);
    testNode(thisNode, "This", "SBJ");
    DependencyNode testNode = isNode.getChildren(1);
    testNode(testNode, "test", "PRD");
    DependencyNode dotNode = isNode.getChildren(2);
    testNode(dotNode, ".", "P");
    DependencyNode aNode = testNode.getChildren(0);
    testNode(aNode, "a", "NMOD");

    jCas.reset();
    tokenBuilder
        .buildTokens(
            jCas,
            "The epicenter was 90 kilometres (55 miles) west-northwest of Chengdu, the capital of Sichuan, with a depth of 19 kilometres (12 mi).",
            "The epicenter was 90 kilometres ( 55 miles ) west-northwest of Chengdu , the capital of Sichuan , with a depth of 19 kilometres ( 12 mi ) .",
            "DT NN VBD VBN IN ( NN NNS ) NN IN NN , DT NN IN NN , IN DT NN IN CD NNS ( CD JJ ) .");
    lemmatizer.process(jCas);
    parser.process(jCas);

    sentence = JCasUtil.selectByIndex(jCas, Sentence.class, 0);
    topNode = AnnotationRetrieval.getContainingAnnotation(jCas, sentence, DependencyNode.class);
    testNode(
        topNode,
        "The epicenter was 90 kilometres (55 miles) west-northwest of Chengdu, the capital of Sichuan, with a depth of 19 kilometres (12 mi).",
        "#$ROOT$#");
    DependencyNode wasNode = topNode.getChildren(0);
    testNode(wasNode, "was", "ROOT");
    testNode(wasNode.getChildren(0), "epicenter", "SBJ");
    testNode(wasNode.getChildren(0).getChildren(0), "The", "NMOD");
    testNode(wasNode.getChildren(0).getChildren(1), "capital", "APPO");
    testNode(wasNode.getChildren(1), "90", "VC");
    testNode(wasNode.getChildren(1).getChildren(0), "kilometres", "ADV");
    testNode(wasNode.getChildren(2), ")", "P");
    testNode(wasNode.getChildren(2).getChildren(0), ",", "P");
    testNode(wasNode.getChildren(2).getChildren(1), "(", "P");
    testNode(wasNode.getChildren(2).getChildren(2), "12", "NMOD");
    testNode(wasNode.getChildren(2).getChildren(3), "mi", "NMOD");
    testNode(wasNode.getChildren(3), ".", "P");

  }

  private void testNode(DependencyNode node, String expectedText, String expectedDependencyType) {
    assertEquals(expectedText, node.getCoveredText());
    assertEquals(expectedDependencyType, node.getDependencyType());

  }
}
