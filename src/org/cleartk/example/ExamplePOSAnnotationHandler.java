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
package org.cleartk.example;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.AnnotationHandler;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.InstanceConsumer;
import org.cleartk.classifier.feature.WindowFeature;
import org.cleartk.classifier.feature.extractor.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.SpannedTextExtractor;
import org.cleartk.classifier.feature.extractor.TypePathExtractor;
import org.cleartk.classifier.feature.extractor.WindowExtractor;
import org.cleartk.classifier.feature.proliferate.CapitalTypeProliferator;
import org.cleartk.classifier.feature.proliferate.CharacterNGramProliferator;
import org.cleartk.classifier.feature.proliferate.LowerCaseProliferator;
import org.cleartk.classifier.feature.proliferate.NumericTypeProliferator;
import org.cleartk.classifier.feature.proliferate.ProliferatingExtractor;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.AnnotationRetrieval;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * 
 * @author Steven Bethard
 */
public class ExamplePOSAnnotationHandler implements AnnotationHandler<String> {

	private List<SimpleFeatureExtractor> tokenFeatureExtractors;
	private List<WindowExtractor> tokenSentenceFeatureExtractors;
	
	public void initialize(UimaContext context) throws ResourceInitializationException{
		// a list of feature extractors that require only the token
		this.tokenFeatureExtractors = new ArrayList<SimpleFeatureExtractor>();
		
		// a list of feature extractors that require the token and the sentence
		this.tokenSentenceFeatureExtractors = new ArrayList<WindowExtractor>();
		
		// basic feature extractors for word, stem and part-of-speech
		SimpleFeatureExtractor wordExtractor, stemExtractor, posExtractor;
		wordExtractor = new SpannedTextExtractor();
		stemExtractor = new TypePathExtractor(Token.class, "stem");
		posExtractor = new TypePathExtractor(Token.class, "pos");
		
		// aliases for NGram feature parameters
		int fromLeft = CharacterNGramProliferator.LEFT_TO_RIGHT;
		int fromRight = CharacterNGramProliferator.RIGHT_TO_LEFT;
		
		// add the feature extractor for the word itself
		// also add proliferators which create new features from the word text
		this.tokenFeatureExtractors.add(new ProliferatingExtractor(
				wordExtractor,
				new LowerCaseProliferator(),
				new CapitalTypeProliferator(),
				new NumericTypeProliferator(),
				new CharacterNGramProliferator(fromLeft, 0, 2),
				new CharacterNGramProliferator(fromLeft, 0, 3),
				new CharacterNGramProliferator(fromRight, 0, 2),
				new CharacterNGramProliferator(fromRight, 0, 3)));
		
		// add the feature extractors for the stem and part of speech
		this.tokenFeatureExtractors.add(stemExtractor);
		
		// add 2 stems to the left and right
		this.tokenSentenceFeatureExtractors.add(new WindowExtractor(
				Token.class, stemExtractor, WindowFeature.ORIENTATION_LEFT, 0, 2));
		this.tokenSentenceFeatureExtractors.add(new WindowExtractor(
				Token.class, stemExtractor, WindowFeature.ORIENTATION_RIGHT, 0, 2));
		
		// add 3 part of speech tags to the left
		this.tokenSentenceFeatureExtractors.add(new WindowExtractor(
				Token.class, posExtractor, WindowFeature.ORIENTATION_LEFT, 0, 3));
		
	}
	
	public void process(JCas jCas, InstanceConsumer<String> consumer) throws AnalysisEngineProcessException {
		
		// generate a list of training instances for each sentence in the document
		for (Sentence sentence: AnnotationRetrieval.getAnnotations(jCas, Sentence.class)) {
			List<Instance<String>> instances = new ArrayList<Instance<String>>();
			List<Token> tokens = AnnotationRetrieval.getAnnotations(jCas, sentence, Token.class);
			
			// for each token, extract all feature values and the label
			for (Token token: tokens) {
				Instance<String> instance = new Instance<String>();
				
				// extract all features that require only the token annotation
				for (SimpleFeatureExtractor extractor: this.tokenFeatureExtractors) {
					instance.addAll(extractor.extract(jCas, token));
				}
				
				// extract all features that require the token and sentence annotations
				for (WindowExtractor extractor: this.tokenSentenceFeatureExtractors) {
					instance.addAll(extractor.extract(jCas, token, sentence));
				}
				
				// set the instance label from the token's part of speech
				instance.setOutcome(token.getPos());
				
				// add the instance to the list
				instances.add(instance);
			}
			
			// pass the instance list to the consumer and get back the list of labels
			List<String> labels = consumer.consumeSequence(instances);
			if (labels != null) {
				
				// if the consumer returned labels, set each token's POS label
				Iterator<Token> tokensIter = tokens.iterator();
				for (String label: labels) {
					tokensIter.next().setPos(label.toString());
				}
			}
		}
	}
}