/** 
 * Copyright (c) 2009, Regents of the University of Colorado 
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

package org.cleartk.examples.pos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.testing.util.HideOutput;
import org.apache.uima.fit.util.JCasUtil;
import org.cleartk.examples.ExamplesTestBase;
import org.cleartk.ml.jar.JarClassifierBuilder;
import org.cleartk.token.type.Token;
import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * 
 */
public class ExampleModelTest extends ExamplesTestBase {

  @Test
  public void testModel() throws Exception {
    HideOutput hider = new HideOutput();
    BuildTestExamplePosModel.main();
    hider.restoreOutput();

    AnalysisEngineDescription posTaggerDescription = ExamplePosAnnotator.getClassifierDescription(JarClassifierBuilder.getModelJarFile(
        ExamplePosAnnotator.DEFAULT_OUTPUT_DIRECTORY).getPath());
    AnalysisEngine posTagger = AnalysisEngineFactory.createEngine(posTaggerDescription);

    tokenBuilder.buildTokens(
        jCas,
        "What would you do if I sang in tune?  Would you listen then?",
        "What would you do if I sang in tune ?\n  Would you listen then ?");

    Token token = JCasUtil.selectByIndex(jCas, Token.class, 0);
    assertNull(token.getPos());
    token = JCasUtil.selectByIndex(jCas, Token.class, 5);
    assertNull(token.getPos());

    posTagger.process(jCas);
    token = JCasUtil.selectByIndex(jCas, Token.class, 0);
    assertNotNull(token.getPos());
    assertEquals("IN", token.getPos());
    token = JCasUtil.selectByIndex(jCas, Token.class, 5);
    assertNotNull(token.getPos());
    assertEquals("NN", token.getPos());
  }
}
