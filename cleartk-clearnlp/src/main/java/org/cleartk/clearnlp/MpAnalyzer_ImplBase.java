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

import java.net.URI;
import java.net.URL;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

import com.googlecode.clearnlp.component.AbstractComponent;
import com.googlecode.clearnlp.dependency.DEPNode;
import com.googlecode.clearnlp.dependency.DEPTree;
import com.googlecode.clearnlp.engine.EngineGetter;
import com.googlecode.clearnlp.nlp.NLPDecode;
import com.googlecode.clearnlp.nlp.NLPLib;
import com.googlecode.clearnlp.reader.AbstractReader;

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * This class provides a base class for wrapping the ClearNLP morphological analyzer into a UIMA based type system. This engine
 * requires POS-tagged tokens and produces lemmatized forms of said tokens.
 * 
 * Subclasses should override the abstract methods to produce the annotations relevant for the target type system.
 * 
 * This analyzer is available here:
 * <p>
 * http://clearnlp.googlecode.com
 * <p>
* 
 * @author Lee Becker
 * 
 */
public abstract class MpAnalyzer_ImplBase<TOKEN_TYPE extends Annotation> extends JCasAnnotator_ImplBase {
	
	public static final String DEFAULT_DICTIONARY_FILE_NAME= "dictionary-1.2.0.zip";
	
    public static final String PARAM_LANGUAGE_CODE = "languageCode";
    
    @ConfigurationParameter(
        name = PARAM_LANGUAGE_CODE,
    		description = "Language code (default value=en).",
    		defaultValue= AbstractReader.LANG_EN)
    private String languageCode;
	
	
	public static final String PARAM_DICTIONARY_URI = "dictionaryUri";
	
	@ConfigurationParameter(
	    name = PARAM_DICTIONARY_URI,
			description = "This parameter provides the URI to the morphological analyzer dictionary used for lemmatizing.")
	private URI dictionaryUri;
	
	public static final String PARAM_WINDOW_CLASS = "windowClass"; 
  
  private static final String WINDOW_TYPE_DESCRIPTION = "specifies the class type of annotations that will be tokenized. "
      + "By default, the tokenizer will tokenize a document sentence by sentence.  If you do not want to precede tokenization with"
      + "sentence segmentation, then a reasonable value for this parameter is 'org.apache.uima.jcas.tcas.DocumentAnnotation'";

  @ConfigurationParameter(
      name = PARAM_WINDOW_CLASS,
      description = WINDOW_TYPE_DESCRIPTION,
      defaultValue = "org.cleartk.token.type.Sentence")
  private Class<? extends Annotation> windowClass;

	/**
	 * Convenience method to create Analysis Engine for ClearNLP's POSTagger + Lemmatizer using default English models and dictionaries.
	 */
	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(MpAnalyzer_ImplBase.class);
	}
	
	public static AnalysisEngineDescription getDescription(String langCode, URI dictionaryUri) throws ResourceInitializationException {
	  return AnalysisEngineFactory.createPrimitiveDescription(MpAnalyzer_ImplBase.class, 
	      MpAnalyzer_ImplBase.PARAM_LANGUAGE_CODE, langCode,
	      MpAnalyzer_ImplBase.PARAM_DICTIONARY_URI, dictionaryUri);
	  
	}
	
  private TokenOps<TOKEN_TYPE> tokenOps; 
  
  public MpAnalyzer_ImplBase(TokenOps<TOKEN_TYPE> tokenOps) {
    this.tokenOps = tokenOps;
  }
	
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		
		try {
		  URL mpAnalyzerDictionaryURL = (this.dictionaryUri == null) 
		      ? MpAnalyzer_ImplBase.class.getResource(DEFAULT_DICTIONARY_FILE_NAME).toURI().toURL()
		      : dictionaryUri.toURL();

		  // initialize ClearNLP components
		  this.mpAnalyzer  = EngineGetter.getComponent(mpAnalyzerDictionaryURL.openStream(), languageCode, NLPLib.MODE_MORPH);
		  this.clearNlpDecoder = new NLPDecode();

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
		  DEPTree depTree = this.clearNlpDecoder.toDEPTree(tokenStrings);
		  for (int i = 1; i < depTree.size(); i++) {
		    TOKEN_TYPE token = tokens.get(i-1);
		    DEPNode node = depTree.get(i);
		    node.pos = this.tokenOps.getPos(jCas, token);
		  }
		  // Run the morphological analyzer
		  this.mpAnalyzer.process(depTree);
		  
		  // Pull out lemmas and stuff them back into the CAS tokens
		  for (int i = 1; i < depTree.size(); i++) {
		    TOKEN_TYPE token = tokens.get(i-1);
		    DEPNode node = depTree.get(i);
		    this.tokenOps.setLemma(jCas, token, node.lemma);
		  }
		}
	}
	
	private AbstractComponent mpAnalyzer;
	private NLPDecode clearNlpDecoder;

}
