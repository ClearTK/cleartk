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

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.TypeCapability;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.util.JCasUtil;

import com.googlecode.clearnlp.engine.EngineGetter;
import com.googlecode.clearnlp.morphology.AbstractMPAnalyzer;
import com.googlecode.clearnlp.reader.AbstractReader;

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * This class provides a UIMA/ClearTK wrapper for the ClearNLP morphological analyzer. This engine
 * requires POS-tagged tokens and produces lemmatized forms of said tokens.
 * 
 * This analyzer is available here:
 * <p>
 * http://clearnlp.googlecode.com
 * <p>
* 
 * @author Lee Becker
 * 
 */
@TypeCapability(
    inputs = { "org.cleartk.token.type.Token:pos" },
    outputs = {"org.cleartk.token.type.Token:lemma"})
public class MPAnalyzer extends JCasAnnotator_ImplBase {
	
	public static final String DEFAULT_DICTIONARY_FILE_NAME= "dictionary-1.2.0.zip";
	
    public static final String PARAM_LANGUAGE_CODE = ConfigurationParameterFactory.createConfigurationParameterName(
    		MPAnalyzer.class,
    		"languageCode");
    
    @ConfigurationParameter(
    		description = "Language code (default value=en).",
    		defaultValue= AbstractReader.LANG_EN)
    private String languageCode;
	
	
	public static final String PARAM_DICTIONARY_URI = ConfigurationParameterFactory.createConfigurationParameterName(
			MPAnalyzer.class, 
			"dictionaryUri");
	
	@ConfigurationParameter(
			description = "This parameter provides the URI to the morphological analyzer dictionary used for lemmatizing.")
	private URI dictionaryUri;
	
	/**
	 * Convenience method to create Analysis Engine for ClearNLP's POSTagger + Lemmatizer using default English models and dictionaries.
	 */
	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(MPAnalyzer.class);
	}
	
	public static AnalysisEngineDescription getDescription(String langCode, URI dictionaryUri) throws ResourceInitializationException {
	  return AnalysisEngineFactory.createPrimitiveDescription(MPAnalyzer.class, 
	      MPAnalyzer.PARAM_LANGUAGE_CODE, langCode,
	      MPAnalyzer.PARAM_DICTIONARY_URI, dictionaryUri);
	  
	}
	
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		
		try {
		  URL mpAnalyzerDictionaryURL = (this.dictionaryUri == null) 
		      ? MPAnalyzer.class.getResource(DEFAULT_DICTIONARY_FILE_NAME).toURI().toURL()
		      : dictionaryUri.toURL();

		  this.mpAnalyzer = EngineGetter.getMPAnalyzer(this.languageCode, mpAnalyzerDictionaryURL.openStream());

		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
		
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
		  for (Token token : JCasUtil.selectCovered(jCas, Token.class, sentence))	{
		    token.setLemma(this.mpAnalyzer.getLemma(token.getCoveredText(), token.getPos()));
		  }
		}
	}
	
	private AbstractMPAnalyzer mpAnalyzer;

}
