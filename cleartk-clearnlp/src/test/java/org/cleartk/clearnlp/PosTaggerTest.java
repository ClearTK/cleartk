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
package org.cleartk.clearnlp;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;


import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.test.CleartkTestBase;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.junit.Test;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.testing.factory.TokenBuilder;
import org.uimafit.util.JCasUtil;

public class PosTaggerTest extends CleartkTestBase {
	protected TokenBuilder<Token, Sentence> tokenBuilder;
	protected AnalysisEngineDescription posTagger;

	protected void initLowMemModel() throws ResourceInitializationException {
	  this.posTagger = PosTagger.getDescription("en", new File("src/test/resources/models/sample-en-pos-1.3.0.tgz").toURI());
	} 
	
	protected void initDefaultModel() throws ResourceInitializationException {
		  this.posTagger = PosTagger.getDescription();
	} 

  @Test
  public void posTaggerLowMemTest() throws Exception {
    initLowMemModel();
    
    
    this.jCas.reset();
    tokenBuilder = new TokenBuilder<Token, Sentence>(Token.class, Sentence.class, "pos", "stem");

    this.tokenBuilder.buildTokens(
        this.jCas,
        "The brown fox jumped quickly over the lazy dog .",
        "The brown fox jumped quickly over the lazy dog .");
    SimplePipeline.runPipeline(jCas, posTagger);
    
		//List<String> expected = Arrays.asList("DT NN IN VBN RB IN DT NN NN .".split(" "));
		List<String> expected = Arrays.asList("DT NN IN JJ NNS CC DT JJ NN .".split(" "));
    List<String> actual = new ArrayList<String>();
    for (Token token : JCasUtil.select(this.jCas, Token.class)) {
      actual.add(token.getPos());
    }
    Assert.assertEquals(expected, actual);
  }	
	
	

	@Test
	public void posTaggerTest() throws Exception {
    this.assumeBigMemoryTestsEnabled();
    this.logger.info(BIG_MEMORY_TEST_MESSAGE);
    
	  initDefaultModel();
		this.jCas.reset();
		tokenBuilder = new TokenBuilder<Token, Sentence>(Token.class, Sentence.class, "pos", "stem");

		this.tokenBuilder.buildTokens(
				this.jCas,
				"The brown fox jumped quickly over the lazy dog .",
				"The brown fox jumped quickly over the lazy dog .");
		SimplePipeline.runPipeline(jCas, posTagger);
		
    List<String> expected = Arrays.asList("DT JJ NN VBD RB IN DT JJ NN .".split(" "));
		List<String> actual = new ArrayList<String>();
		for (Token token : JCasUtil.select(this.jCas, Token.class)) {
			actual.add(token.getPos());
		}
		Assert.assertEquals(expected, actual);
	}
	
}
