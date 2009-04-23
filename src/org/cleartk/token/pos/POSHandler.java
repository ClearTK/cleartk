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
package org.cleartk.token.pos;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.SequentialAnnotationHandler;
import org.cleartk.classifier.SequentialInstanceConsumer;
import org.cleartk.util.ReflectionUtil;
import org.cleartk.util.UIMAUtil;

/**
 * <br>Copyright (c) 2009, Regents of the University of Colorado 
 * <br>All rights reserved.
 *
 * @author Philip Ogren
 *
 */

public abstract class POSHandler<TOKEN_TYPE extends Annotation, SENTENCE_TYPE extends Annotation> implements
		SequentialAnnotationHandler<String> {

	public static final String PARAM_FEATURE_EXTRACTOR_CLASS = "org.cleartk.token.pos.POSHandler.PARAM_FEATURE_EXTRACTOR_CLASS";

	public static final String PARAM_TAGGER_CLASS = "org.cleartk.token.pos.POSHandler.PARAM_TAGGER_CLASS";

	protected boolean typesInitialized = false;

	protected POSFeatureExtractor<TOKEN_TYPE, SENTENCE_TYPE> featureExtractor;

	protected POSTagger<TOKEN_TYPE> tagger;

	private java.lang.reflect.Type tokenClassType; 
	protected Type tokenType;

	private java.lang.reflect.Type sentenceClassType;
	protected Type sentenceType;


	public void initialize(UimaContext context) throws ResourceInitializationException {
		try {
			tokenClassType = ReflectionUtil.getTypeArgument(POSHandler.class, "TOKEN_TYPE", this);
			sentenceClassType = ReflectionUtil.getTypeArgument(POSHandler.class, "SENTENCE_TYPE", this);
			
			featureExtractor = ReflectionUtil.uncheckedCast(UIMAUtil.create(
					context, PARAM_FEATURE_EXTRACTOR_CLASS, POSFeatureExtractor.class));
			UIMAUtil.initialize(featureExtractor, context);
			
			java.lang.reflect.Type featureExtractorTokenClassType = ReflectionUtil.getTypeArgument(POSFeatureExtractor.class, "TOKEN_TYPE", featureExtractor);
			
			if (!ReflectionUtil.isAssignableFrom(featureExtractorTokenClassType, tokenClassType)) {
				throw new RuntimeException(String.format(
						"feature extractor token type, %s, is incompatible with POS handler token type, %s.",
						featureExtractorTokenClassType, tokenClassType));
			}

			java.lang.reflect.Type featureExtractorSentenceClassType = ReflectionUtil.getTypeArgument(POSFeatureExtractor.class, "SENTENCE_TYPE", featureExtractor);
			if (!ReflectionUtil.isAssignableFrom(featureExtractorSentenceClassType, sentenceClassType)) {
				throw new RuntimeException(String.format(
						"feature extractor sentence type, %s, is incompatible with POS handler sentence type, %s.",
						featureExtractorSentenceClassType, sentenceClassType));
			}

			tagger = ReflectionUtil.uncheckedCast(UIMAUtil.create(context, PARAM_TAGGER_CLASS, POSTagger.class));
			
			java.lang.reflect.Type taggerTokenClassType = ReflectionUtil.getTypeArgument(POSTagger.class, "TOKEN_TYPE", tagger);
			if (!ReflectionUtil.isAssignableFrom(taggerTokenClassType, tokenClassType)) {
				throw new RuntimeException(String.format(
						"tagger token type, %s, is incompatible with POS handler token type, %s.",
						taggerTokenClassType, tokenClassType));
			}

		}
		catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
	}

	protected void initializeTypes(JCas jCas) throws AnalysisEngineProcessException {
		try {
			tokenType = UIMAUtil.getCasType(jCas, ReflectionUtil.<Class<? extends TOP>>uncheckedCast(tokenClassType));
			sentenceType = UIMAUtil.getCasType(jCas, ReflectionUtil.<Class<? extends TOP>>uncheckedCast(sentenceClassType));
		}
		catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}
		typesInitialized = true;
	}

	public void process(JCas jCas, SequentialInstanceConsumer<String> consumer) throws AnalysisEngineProcessException, CleartkException {
		if (!typesInitialized) initializeTypes(jCas);
		
		FSIterator sentences = jCas.getAnnotationIndex(sentenceType).iterator();
		while (sentences.hasNext()) {
			SENTENCE_TYPE sentence = ReflectionUtil.uncheckedCast(sentences.next());

			List<Instance<String>> instances = new ArrayList<Instance<String>>();
			
			FSIterator tokens = jCas.getAnnotationIndex(tokenType).subiterator(sentence);

			while (tokens.hasNext()) {
				TOKEN_TYPE token = ReflectionUtil.uncheckedCast(tokens.next());
				List<Feature> features = featureExtractor.extractFeatures(jCas, token, sentence);
				Instance<String> instance = new Instance<String>();
				instance.addAll(features);
				instance.setOutcome(tagger.getTag(jCas, token));
				instances.add(instance);
			}

			List<String> tags = consumer.consumeSequence(instances);
			if (tags != null) {
				tokens.moveToFirst();
				for(int i=0; tokens.hasNext(); i++) {
					TOKEN_TYPE token = ReflectionUtil.uncheckedCast(tokens.next());
					tagger.setTag(jCas, token, tags.get(i));
				}
			}
		}
	}

	
}
