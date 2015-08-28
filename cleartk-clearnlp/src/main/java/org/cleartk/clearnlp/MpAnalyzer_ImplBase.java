/*
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
package org.cleartk.clearnlp;

import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;

import com.google.common.annotations.Beta;
//import com.clearnlp.component.AbstractComponent;
//import com.clearnlp.dependency.DEPNode;
//import com.clearnlp.dependency.DEPTree;
//import com.clearnlp.nlp.NLPGetter;
//import com.clearnlp.nlp.NLPMode;
//import com.clearnlp.reader.AbstractReader;





import edu.emory.clir.clearnlp.component.mode.morph.AbstractMPAnalyzer;
import edu.emory.clir.clearnlp.component.utils.NLPUtils;
import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.util.lang.TLanguage;

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * This class provides a base class for wrapping the ClearNLP morphological analyzer into a UIMA
 * based type system. This engine requires POS-tagged tokens and produces lemmatized forms of said
 * tokens.
 * 
 * Subclasses should override the abstract methods to produce the annotations relevant for the
 * target type system.
 * 
 * This analyzer is available here:
 * <p>
 * http://clearnlp.googlecode.com
 * <p>
 * 
 * @author Lee Becker
 * 
 */
@Beta
public abstract class MpAnalyzer_ImplBase<TOKEN_TYPE extends Annotation> extends
    JCasAnnotator_ImplBase {

  public static final String DEFAULT_DICTIONARY_FILE_NAME = "dictionary-1.2.0.zip";

  public static final String PARAM_LANGUAGE_CODE = "languageCode";

  @ConfigurationParameter(
      name = PARAM_LANGUAGE_CODE,
      mandatory = false,
      description = "Language code (default value=en).",
      defaultValue = "ENGLISH")
  private String languageCode;

  public static final String PARAM_WINDOW_CLASS = "windowClass";

  private static final String WINDOW_TYPE_DESCRIPTION = "specifies the class type of annotations that will be tokenized. "
      + "By default, the tokenizer will tokenize a document sentence by sentence.  If you do not want to precede tokenization with"
      + "sentence segmentation, then a reasonable value for this parameter is 'org.apache.uima.jcas.tcas.DocumentAnnotation'";

  @ConfigurationParameter(
      name = PARAM_WINDOW_CLASS,
      mandatory = false,
      description = WINDOW_TYPE_DESCRIPTION,
      defaultValue = "org.cleartk.token.type.Sentence")
  private Class<? extends Annotation> windowClass;

  /**
   * Convenience method to create Analysis Engine for ClearNLP's POSTagger + Lemmatizer using
   * default English models and dictionaries.
   */
  public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
    return AnalysisEngineFactory.createEngineDescription(MpAnalyzer_ImplBase.class);
  }

  public static AnalysisEngineDescription getDescription(String langCode) 
      throws ResourceInitializationException {
    return AnalysisEngineFactory.createEngineDescription(
        MpAnalyzer_ImplBase.class,
        MpAnalyzer_ImplBase.PARAM_LANGUAGE_CODE,
        langCode);

  }

  private TokenOps<TOKEN_TYPE> tokenOps;

  public MpAnalyzer_ImplBase(TokenOps<TOKEN_TYPE> tokenOps) {
    this.tokenOps = tokenOps;
  }

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    try {

      // initialize ClearNLP components
      this.mpAnalyzer = NLPUtils.getMPAnalyzer(TLanguage.getType(languageCode));
      
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }

  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    for (Annotation window : JCasUtil.select(jCas, this.windowClass)) {
      List<TOKEN_TYPE> tokens = this.tokenOps.selectTokens(jCas, window);
      List<String> tokenStrings = JCasUtil.toText(tokens);

      // All processing in ClearNLP goes through the DEPTree structures,
      // so populate it with token and POS tag info
      DEPTree depTree = new DEPTree(tokenStrings);
      for (int i = 1; i < depTree.size(); i++) {
        TOKEN_TYPE token = tokens.get(i - 1);
        DEPNode node = depTree.get(i);
        node.setPOSTag(this.tokenOps.getPos(jCas, token));
      }
      // Run the morphological analyzer
      this.mpAnalyzer.process(depTree);

      // Pull out lemmas and stuff them back into the CAS tokens
      for (int i = 1; i < depTree.size(); i++) {
        TOKEN_TYPE token = tokens.get(i - 1);
        DEPNode node = depTree.get(i);
        this.tokenOps.setLemma(jCas, token, node.getLemma());
      }
    }
  }

  private AbstractMPAnalyzer mpAnalyzer;

}
