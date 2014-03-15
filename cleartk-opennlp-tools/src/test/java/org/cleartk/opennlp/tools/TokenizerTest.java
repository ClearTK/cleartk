/** 
 * Copyright (c) 2012, Regents of the University of Colorado 
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
package org.cleartk.opennlp.tools;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.testing.util.DisableLogging;
import org.apache.uima.fit.util.JCasUtil;
import org.cleartk.opennlp.tools.Tokenizer;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.junit.Assert;
import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 */
public class TokenizerTest extends OpennlpTestBase {

  @Test
  public void test() throws Exception {
    // note that the OpenNLP tokenizer isn't as good as the ClearTK one, so this is simpler than
    // org.cleartk.token.tokenizer.TokenizerAndTokenAnnotatorTest.testMarysDog
    this.jCas.setDocumentText("\"John & Mary's dog,\" Jane thought (to herself).\n"
        + "\"What a #$%!\n" + "a- ``I like AT&T''.\"");
    new Sentence(this.jCas, 0, 47).addToIndexes();
    new Sentence(this.jCas, 48, 60).addToIndexes();
    new Sentence(this.jCas, 61, 81).addToIndexes();
    Level level = DisableLogging.disableLogging();
    AnalysisEngine engine = AnalysisEngineFactory.createEngine(Tokenizer.getDescription("en"));
    engine.process(this.jCas);
    DisableLogging.enableLogging(level);
    List<String> expected = Arrays.asList(
        "\"",
        "John",
        "&",
        "Mary",
        "'s",
        "dog",
        ",",
        "\"",
        "Jane",
        "thought",
        "(",
        "to",
        "herself",
        ")",
        ".",
        "\"",
        "What",
        "a",
        "#",
        "$",
        "%",
        "!",
        "a",
        "-",
        "``",
        "I",
        "like",
        "AT&T",
        "''",
        ".",
        "\"");
    List<String> actual = JCasUtil.toText(JCasUtil.select(this.jCas, Token.class));
    Assert.assertEquals(expected, actual);
  }
}
