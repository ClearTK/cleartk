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

import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;

import com.google.common.annotations.Beta;
import com.google.common.collect.Lists;

import edu.emory.clir.clearnlp.component.utils.NLPUtils;
import edu.emory.clir.clearnlp.tokenization.AbstractTokenizer;
import edu.emory.clir.clearnlp.util.lang.TLanguage;

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * This class provides a wrapper for the ClearNLP part of speech tokenizer for UIMA and/or ClearTK
 * type systems.
 * 
 * Subclasses should override the abstract methods to produce the annotations relevant for the
 * target type system.
 * 
 * This tagger is available here:
 * <p>
 * http://clearnlp.googlecode.com
 * <p>
 * 
 * @author Lee Becker
 * 
 */
@Beta
public abstract class Tokenizer_ImplBase<TOKEN_TYPE extends Annotation, SENTENCE_TYPE extends Annotation> extends
    JCasAnnotator_ImplBase {
  
  
  public static final String PARAM_SEGMENT_SENTENCES = "segmentSentences";
  @ConfigurationParameter(
      name = PARAM_SEGMENT_SENTENCES,
      mandatory = false,
      description = "Turn on flag to include sentence segmentation",
      defaultValue = "false")
  private Boolean segmentSentences;

  public static final String PARAM_LANGUAGE_CODE = "languageCode";

  @ConfigurationParameter(
      name = PARAM_LANGUAGE_CODE,
      mandatory = false,
      description = "Language code for the tokenizer (default value=en).",
      defaultValue = "ENGLISH")
  private String languageCode;

  public static final String PARAM_WINDOW_CLASS = "windowClass";

  private static final String WINDOW_TYPE_DESCRIPTION = "specifies the class type of annotations that will be tokenized. "
      + "By default, the tokenizer will tokenize a document sentence by sentence.  If you do not want to precede tokenization with"
      + "sentence segmentation, then a reasonable value for this parameter is 'org.apache.uima.jcas.tcas.DocumentAnnotation'";

  @ConfigurationParameter(
      name = PARAM_WINDOW_CLASS,
      mandatory = false,
      description = WINDOW_TYPE_DESCRIPTION)
      //defaultValue = "org.cleartk.token.type.Sentence")
  private Class<? extends Annotation> windowClass;

  private AbstractTokenizer tokenizer;

  protected abstract TokenOps<TOKEN_TYPE> getTokenOps();
  protected abstract SentenceOps<SENTENCE_TYPE> getSentenceOps();

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    try {
      this.tokenizer = NLPUtils.getTokenizer(TLanguage.getType(languageCode));
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    
    
    if (this.segmentSentences) {
      // Run combined Sentence Segmentation and Tokenization
      if (this.windowClass == null) {
        // If no windowClass specified, run combined segmentation and tokenization on the entire document
        this.segmentSentencesAndTokenizeText(jCas, jCas.getDocumentText(), 0);
      } else {
        // If windowClass specified, run only tokenization over the window type
        for (Annotation window : JCasUtil.select(jCas, this.windowClass)) {
          this.segmentSentencesAndTokenizeText(jCas, window.getCoveredText(), window.getBegin());
        }
      }
    } else {
      // Run Tokenization Only
      if (this.windowClass == null) {
        // If no windowClass specified, run tokenization on the entire document
        this.tokenizeText(jCas, jCas.getDocumentText(), 0);
      } else {

        // Run tokenization over windowClass
        for (Annotation window : JCasUtil.select(jCas, this.windowClass)) {
          this.tokenizeText(jCas, window.getCoveredText(), window.getBegin());
        }
      }
    }
    
    
  }
  
  void segmentSentencesAndTokenizeText(JCas jCas, String text, int textOffset) throws AnalysisEngineProcessException {
    InputStream stream = IOUtils.toInputStream(text);
    List<List<String>> sentencesTokens = this.tokenizer.segmentize(stream);

    int offset = textOffset;
    for (List<String> sentenceTokenStrings : sentencesTokens) {
      int sentenceBegin = -1;
      int sentenceEnd = -1;
      List <TOKEN_TYPE> tokens = Lists.newArrayList();
      for (String token : sentenceTokenStrings) {
        int tokenBegin = text.indexOf(token, offset);
        int tokenEnd = tokenBegin + token.length();
        try {
          TOKEN_TYPE t = this.getTokenOps().createToken(jCas, tokenBegin, tokenEnd);
          tokens.add(t);
        } catch (Exception e) {
          throw new AnalysisEngineProcessException(e);
        }
        if (sentenceBegin < 0) {
          sentenceBegin = tokenBegin;
        }
        offset = tokenEnd;
        sentenceEnd = tokenEnd;
      }
      if (sentenceBegin >= 0 && sentenceEnd >= 0) {
        this.getSentenceOps().createSentence(jCas, sentenceBegin, sentenceEnd);
      }
    }
  }
  
  void tokenizeText(JCas jCas, String text, int textOffset) throws AnalysisEngineProcessException  {
    List<String> tokens = tokenizer.tokenize(text);
    

    int offset = 0;
    for (String token : tokens) {
      int tokenBegin = text.indexOf(token, offset);
      int tokenEnd = tokenBegin + token.length();
      try {
        this.getTokenOps().createToken(jCas, textOffset + tokenBegin, textOffset + tokenEnd);
      } catch (Exception e) {
        throw new AnalysisEngineProcessException(e);
      }
      offset = tokenEnd;
    }
    
  }

}
