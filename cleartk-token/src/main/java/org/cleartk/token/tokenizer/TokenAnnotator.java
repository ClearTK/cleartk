/** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
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
package org.cleartk.token.tokenizer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.factory.initializable.InitializableFactory;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 * 
 * @author Philip Ogren
 * 
 */
public class TokenAnnotator extends JCasAnnotator_ImplBase {

  public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(TokenAnnotator.class);
  }

  public static final String PARAM_TOKENIZER_NAME = ConfigurationParameterFactory.createConfigurationParameterName(
      TokenAnnotator.class,
      "tokenizerName");

  private static final String TOKENIZER_DESCRIPTION = "specifies the class type of the tokenizer that will be used by this annotator. "
      + "If this parameter is not filled, then the default tokenenizer (org.cleartk.token.util.PennTreebankTokenizer) is used. "
      + "A tokenenizer is defined as any implementation of the interface defined by org.cleartk.token.util.Tokenizer.";

  @ConfigurationParameter(
      description = TOKENIZER_DESCRIPTION,
      defaultValue = "org.cleartk.token.tokenizer.PennTreebankTokenizer")
  private String tokenizerName;

  public static final String PARAM_TOKEN_TYPE_NAME = ConfigurationParameterFactory.createConfigurationParameterName(
      TokenAnnotator.class,
      "tokenTypeName");

  @ConfigurationParameter(
      description = "class type of the tokens that are created by this annotator. If this parameter is not filled, then tokens of type org.cleartk.token.type.Token will be created.",
      defaultValue = "org.cleartk.token.type.Token")
  private String tokenTypeName;

  public static final String PARAM_WINDOW_TYPE_NAME = ConfigurationParameterFactory.createConfigurationParameterName(
      TokenAnnotator.class,
      "windowTypeName");

  private static final String WINDOW_TYPE_DESCRIPTION = "specifies the class type of annotations that will be tokenized. "
      + "By default, the tokenizer will tokenize a document sentence by sentence.  If you do not want to precede tokenization with"
      + "sentence segmentation, then a reasonable value for this parameter is 'org.apache.uima.jcas.tcas.DocumentAnnotation'";

  @ConfigurationParameter(
      description = WINDOW_TYPE_DESCRIPTION,
      defaultValue = "org.cleartk.token.type.Sentence")
  private String windowTypeName;

  Tokenizer tokenizer;

  Class<? extends Annotation> tokenClass;

  Constructor<? extends Annotation> tokenConstructor;

  private Class<? extends Annotation> windowClass;

  private Type windowType = null;

  private boolean typesInitialized = false;

  public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
    try {
      super.initialize(uimaContext);
      tokenizer = InitializableFactory.create(uimaContext, tokenizerName, Tokenizer.class);
      tokenClass = InitializableFactory.getClass(tokenTypeName, Annotation.class);
      tokenConstructor = tokenClass.getConstructor(new Class[] {
          JCas.class,
          Integer.TYPE,
          Integer.TYPE });
      if (windowTypeName != null)
        windowClass = InitializableFactory.getClass(windowTypeName, Annotation.class);
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
  }

  private void initializeTypes(JCas jCas) {
    if (windowClass != null) {
      windowType = JCasUtil.getType(jCas, windowClass);
    }
    typesInitialized = true;
  }

  public void process(JCas jCas) throws AnalysisEngineProcessException {
    try {
      if (!typesInitialized)
        initializeTypes(jCas);
      if (windowType != null) {
        FSIterator<Annotation> windows = jCas.getAnnotationIndex(windowType).iterator();
        while (windows.hasNext()) {
          Annotation window = windows.next();
          List<Token> pojoTokens = tokenizer.getTokens(window.getCoveredText());
          createTokens(pojoTokens, window.getBegin(), jCas);
        }
      } else {
        String text = jCas.getDocumentText();
        List<Token> pojoTokens = tokenizer.getTokens(text);
        createTokens(pojoTokens, 0, jCas);
      }
    } catch (Exception e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

  private void createTokens(List<Token> pojoTokens, int offset, JCas jCas)
      throws InstantiationException, InvocationTargetException, IllegalAccessException {
    for (Token pojoToken : pojoTokens) {
      int tokenBegin = pojoToken.getBegin() + offset;
      int tokenEnd = pojoToken.getEnd() + offset;
      tokenConstructor.newInstance(jCas, tokenBegin, tokenEnd).addToIndexes();
    }
  }

  public void setTokenizerName(String tokenizerName) {
    this.tokenizerName = tokenizerName;
  }

  public void setTokenTypeName(String tokenTypeName) {
    this.tokenTypeName = tokenTypeName;
  }

  public void setWindowTypeName(String windowTypeName) {
    this.windowTypeName = windowTypeName;
  }

}
