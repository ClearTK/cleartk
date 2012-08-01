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

package org.cleartk.token.lemma.choi;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Iterator;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.cleartk.test.CleartkTestBase;
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

public class LemmaAnnotatorTest extends CleartkTestBase {

  protected TokenBuilder<Token, Sentence> tokenBuilder;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    tokenBuilder = new TokenBuilder<Token, Sentence>(Token.class, Sentence.class, "pos", "stem");
  }

  @Test
  public void testLemmaAnnotator() throws Exception {
    AnalysisEngine lemmatizer = AnalysisEngineFactory.createPrimitive(LemmaAnnotator.class);

    tokenBuilder.buildTokens(jCas, "This is a test.", "This is a test .", "DT VB DT NN .");
    lemmatizer.process(jCas);
    testLemmas("this be a test .");

    jCas.reset();
    tokenBuilder.buildTokens(jCas, "took octopi", "took octopi", "VB NN");
    lemmatizer.process(jCas);
    testLemmas("take octopus");
  }

  private void testLemmas(String expectedLemmasString) {
    String[] expectedLemmas = expectedLemmasString.split("\\s+");
    Collection<Token> tokens = JCasUtil.select(jCas, Token.class);
    assertEquals(expectedLemmas.length, tokens.size());
    Iterator<Token> tokensIter = tokens.iterator();
    for (int i = 0; i < tokens.size(); i++) {
      assertEquals(expectedLemmas[i], tokensIter.next().getLemma());
    }
  }

}
