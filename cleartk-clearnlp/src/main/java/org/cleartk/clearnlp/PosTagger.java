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
import com.googlecode.clearnlp.engine.EngineProcess;
import com.googlecode.clearnlp.pos.POSNode;
import com.googlecode.clearnlp.pos.POSTagger;
import com.googlecode.clearnlp.util.pair.Pair;

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * This class provides a UIMA/ClearTK wrapper for the ClearNLP part of speech (POS) tagger. This engine
 * requires tokenize input and produces POS tags on the tokens.
 * 
 * This tagger is available here:
 * <p>
 * http://clearnlp.googlecode.com
 * <p>
* 
 * @author Lee Becker
 * 
 */
@TypeCapability(
    inputs = { "org.cleartk.token.type.Token" },
    outputs = {"org.cleartk.token.type.Token:pos"})
public class PosTagger extends JCasAnnotator_ImplBase {
	
	public static final String DEFAULT_MODEL_FILE_NAME = "ontonotes-en-pos-1.1.0g.jar";
	
	public static final String PARAM_MODEL_URI = ConfigurationParameterFactory.createConfigurationParameterName(
			PosTagger.class,
			"modelUri");
	
	@ConfigurationParameter(
			description = "This parameter provides the URI to the pos tagger model.")
	private URI modelUri;

	
	/**
	 * Convenience method to create Analysis Engine for ClearNLP's POSTagger + Lemmatizer using default English models and dictionaries.
	 */
	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(PosTagger.class);
	}
	
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		
		try {
		  URL modelURL = (this.modelUri == null) 
		      ? PosTagger.class.getResource(DEFAULT_MODEL_FILE_NAME).toURI().toURL()
		      : this.modelUri.toURL();
			this.taggers = EngineGetter.getPOSTaggers(modelURL.openStream());
			
		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
		
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
			List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);	
			if (tokens.size() <= 0) { return; }
			
			List<String> tokenStrings = JCasUtil.toText(tokens);
			
			POSNode[] posNodes = EngineProcess.getPOSNodes(this.taggers, tokenStrings);
					
			for (int i = 0; i < tokens.size(); i++) {
				Token token = tokens.get(i);
				POSNode posNode = posNodes[i];
				token.setPos(posNode.pos);
			}
		}
	}
	
	private Pair<POSTagger[], Double> taggers;

}
