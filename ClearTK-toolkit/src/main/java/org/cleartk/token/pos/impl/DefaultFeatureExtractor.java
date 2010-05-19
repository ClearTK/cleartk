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
package org.cleartk.token.pos.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkException;
import org.cleartk.Initializable;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.WindowFeature;
import org.cleartk.classifier.feature.WindowNGramFeature;
import org.cleartk.classifier.feature.extractor.WindowExtractor;
import org.cleartk.classifier.feature.extractor.WindowNGramExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.SpannedTextExtractor;
import org.cleartk.classifier.feature.proliferate.CapitalTypeProliferator;
import org.cleartk.classifier.feature.proliferate.CharacterNGramProliferator;
import org.cleartk.classifier.feature.proliferate.LowerCaseProliferator;
import org.cleartk.classifier.feature.proliferate.NumericTypeProliferator;
import org.cleartk.classifier.feature.proliferate.ProliferatingExtractor;
import org.cleartk.token.pos.POSFeatureExtractor;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;

/**
 * <br>Copyright (c) 2009, Regents of the University of Colorado 
 * <br>All rights reserved.
 *
 * @author Philip Ogren
 *
 */

public class DefaultFeatureExtractor implements POSFeatureExtractor<Token, Sentence>, Initializable{

	private List<SimpleFeatureExtractor> simpleExtractors;
	private List<WindowExtractor> windowExtractors;
	private List<WindowNGramExtractor> windowNGramExtractors;
	
	public void initialize(UimaContext context) throws ResourceInitializationException {
		simpleExtractors = new ArrayList<SimpleFeatureExtractor>();

		SimpleFeatureExtractor wordExtractor = new SpannedTextExtractor();

		int fromLeft = CharacterNGramProliferator.LEFT_TO_RIGHT;
		int fromRight = CharacterNGramProliferator.RIGHT_TO_LEFT;
		simpleExtractors.add(new ProliferatingExtractor(
				wordExtractor,
				new LowerCaseProliferator(),
				new CapitalTypeProliferator(),
				new NumericTypeProliferator(),
				new CharacterNGramProliferator(fromLeft, 0, 1),
				new CharacterNGramProliferator(fromLeft, 0, 2),
				new CharacterNGramProliferator(fromLeft, 0, 3),
				new CharacterNGramProliferator(fromRight, 0, 1),
				new CharacterNGramProliferator(fromRight, 0, 2),
				new CharacterNGramProliferator(fromRight, 0, 3),
				new CharacterNGramProliferator(fromRight, 0, 4),
				new CharacterNGramProliferator(fromRight, 0, 5),
				new CharacterNGramProliferator(fromRight, 0, 6)));

		windowExtractors = new ArrayList<WindowExtractor>();
		
		windowExtractors.add(new WindowExtractor(
				Token.class, wordExtractor, WindowFeature.ORIENTATION_LEFT, 0, 2));
		windowExtractors.add(new WindowExtractor(
				Token.class, wordExtractor, WindowFeature.ORIENTATION_RIGHT, 0, 2));
		
		windowNGramExtractors = new ArrayList<WindowNGramExtractor>();
		windowNGramExtractors.add(new WindowNGramExtractor(
				Token.class, wordExtractor, WindowNGramFeature.ORIENTATION_LEFT,WindowNGramFeature.DIRECTION_LEFT_TO_RIGHT,"_", 0, 2));
		windowNGramExtractors.add(new WindowNGramExtractor(
				Token.class, wordExtractor, WindowNGramFeature.ORIENTATION_RIGHT,WindowNGramFeature.DIRECTION_LEFT_TO_RIGHT,"_", 0, 2));
	}

	public List<Feature> extractFeatures(JCas jCas, Token token, Sentence sentence) throws CleartkException {
		List<Feature> features = new ArrayList<Feature>();

		for (SimpleFeatureExtractor extractor: simpleExtractors) {
			features.addAll(extractor.extract(jCas, token));
		}
		
		for (WindowExtractor extractor: windowExtractors) {
			features.addAll(extractor.extract(jCas, token, sentence));
		}

		for (WindowNGramExtractor extractor: windowNGramExtractors) {
			features.add(extractor.extract(jCas, token, sentence));
		}
		
		return features;
	}


}
