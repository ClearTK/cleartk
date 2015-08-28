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

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;

import org.apache.uima.fit.factory.AnalysisEngineFactory;

public class Tokenizer extends Tokenizer_ImplBase<Token, Sentence> {
  
  
  private CleartkTokenOps tokenOps;
  private CleartkSentenceOps sentenceOps;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    this.tokenOps = new CleartkTokenOps();
    this.sentenceOps = new CleartkSentenceOps();
  }

  public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
    return getTokenizerDescription("ENGLISH");
  }
  
  public static AnalysisEngineDescription getTokenizerDescription(String languageCode) throws ResourceInitializationException {

    return AnalysisEngineFactory.createEngineDescription(Tokenizer.class,
        Tokenizer_ImplBase.PARAM_LANGUAGE_CODE,
        languageCode,
        Tokenizer_ImplBase.PARAM_WINDOW_CLASS,
        "org.cleartk.token.type.Sentence"
        );
  }

  public static AnalysisEngineDescription getSentenceSegmenterAndTokenizerDescription(String languageCode) throws ResourceInitializationException {

    return AnalysisEngineFactory.createEngineDescription(Tokenizer.class,
        Tokenizer_ImplBase.PARAM_LANGUAGE_CODE,
        languageCode,
        //Tokenizer_ImplBase.PARAM_WINDOW_CLASS,
        //org.cleartk.token.type.Sentence.class,
        Tokenizer_ImplBase.PARAM_SEGMENT_SENTENCES,
        true
        );
  }
  
  
  public static AnalysisEngineDescription getDescription(String languageCode) throws ResourceInitializationException {
    return getTokenizerDescription(languageCode);
  }

  @Override
  protected TokenOps<Token> getTokenOps() {
    return this.tokenOps;
  }

  @Override
  protected SentenceOps<Sentence> getSentenceOps() {
    return this.sentenceOps;
  }
}


