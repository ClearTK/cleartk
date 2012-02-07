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

package org.cleartk.srl.dependency.clear;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.cleartk.srl.type.Predicate;
import org.cleartk.srl.type.SemanticArgument;
import org.cleartk.syntax.dependency.clear.ClearParser;
import org.cleartk.test.CleartkTestBase;
import org.cleartk.token.lemma.choi.LemmaAnnotator;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.cleartk.util.UIMAUtil;
import org.junit.Before;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.testing.factory.TokenBuilder;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * 
 * @author Lee Becker
 */

public class ClearParserSRLTest extends CleartkTestBase {

  @Override
  public String[] getTypeSystemDescriptorNames() {
    return new String[] {
        "org.cleartk.token.TypeSystem",
        "org.cleartk.syntax.dependency.TypeSystem",
        "org.cleartk.srl.TypeSystem" };
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
    AnalysisEngine depparser = AnalysisEngineFactory.createPrimitive(
        ClearParser.class,
        typeSystemDescription);
    AnalysisEngine srlparser = AnalysisEngineFactory.createPrimitive(
        ClearParserSRL.class,
        typeSystemDescription);

    tokenBuilder.buildTokens(
        jCas,
        "John still drives the car Mary gave him in 1978.",
        "John still drives the car Mary gave him in 1978.",
        "NNP  RB    VBZ    DT  NN  NNP  VBD  PRP IN CD  .");
    lemmatizer.process(jCas);
    depparser.process(jCas);
    srlparser.process(jCas);

    Sentence sentence = JCasUtil.selectByIndex(jCas, Sentence.class, 0);

    List<Predicate> predicates = JCasUtil.selectCovered(jCas, Predicate.class, sentence);
    Predicate pred;
    List<SemanticArgument> args;

    // Check first predicate-argument structure
    // should be drives(A0=John, AM-TMP=still, A1=car)
    pred = predicates.get(0);
    assertEquals("drives", pred.getCoveredText());
    args = UIMAUtil.toList(pred.getArguments(), SemanticArgument.class);
    assertEquals("A0", args.get(0).getLabel());
    assertEquals("John", args.get(0).getCoveredText());
    assertEquals("AM-TMP", args.get(1).getLabel());
    assertEquals("still", args.get(1).getCoveredText());
    assertEquals("A1", args.get(2).getLabel());
    assertEquals("car", args.get(2).getCoveredText());

    // Check second predicate-argument structure
    // should be gave(A0=Mary, A2=him, AM-TMP=in)
    pred = predicates.get(1);
    assertEquals("gave", pred.getCoveredText());
    args = UIMAUtil.toList(pred.getArguments(), SemanticArgument.class);
    assertEquals("A0", args.get(0).getLabel());
    assertEquals("Mary", args.get(0).getCoveredText());
    assertEquals("A2", args.get(1).getLabel());
    assertEquals("him", args.get(1).getCoveredText());
    assertEquals("AM-TMP", args.get(2).getLabel());
    assertEquals("in", args.get(2).getCoveredText());
  }

}
