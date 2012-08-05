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
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractor;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Following;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Ngram;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Preceding;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.CoveredTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.function.CapitalTypeFeatureFunction;
import org.cleartk.classifier.feature.function.CharacterNGramFeatureFunction;
import org.cleartk.classifier.feature.function.FeatureFunctionExtractor;
import org.cleartk.classifier.feature.function.LowerCaseFeatureFunction;
import org.cleartk.classifier.feature.function.NumericTypeFeatureFunction;
import org.cleartk.token.pos.POSFeatureExtractor;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.uimafit.factory.initializable.Initializable;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * 
 */

public class DefaultFeatureExtractor implements POSFeatureExtractor<Token, Sentence>, Initializable {

  private List<SimpleFeatureExtractor> simpleExtractors;

  private List<CleartkExtractor> windowExtractors;

  private List<CleartkExtractor> windowNGramExtractors;

  public void initialize(UimaContext context) throws ResourceInitializationException {
    simpleExtractors = new ArrayList<SimpleFeatureExtractor>();

    SimpleFeatureExtractor wordExtractor = new CoveredTextExtractor();

    CharacterNGramFeatureFunction.Orientation fromLeft = CharacterNGramFeatureFunction.Orientation.LEFT_TO_RIGHT;
    CharacterNGramFeatureFunction.Orientation fromRight = CharacterNGramFeatureFunction.Orientation.RIGHT_TO_LEFT;
    simpleExtractors.add(new FeatureFunctionExtractor(
        wordExtractor,
        new LowerCaseFeatureFunction(),
        new CapitalTypeFeatureFunction(),
        new NumericTypeFeatureFunction(),
        new CharacterNGramFeatureFunction(fromLeft, 0, 1),
        new CharacterNGramFeatureFunction(fromLeft, 0, 2),
        new CharacterNGramFeatureFunction(fromLeft, 0, 3),
        new CharacterNGramFeatureFunction(fromRight, 0, 1),
        new CharacterNGramFeatureFunction(fromRight, 0, 2),
        new CharacterNGramFeatureFunction(fromRight, 0, 3),
        new CharacterNGramFeatureFunction(fromRight, 0, 4),
        new CharacterNGramFeatureFunction(fromRight, 0, 5),
        new CharacterNGramFeatureFunction(fromRight, 0, 6)));

    windowExtractors = new ArrayList<CleartkExtractor>();

    windowExtractors.add(new CleartkExtractor(
        Token.class,
        wordExtractor,
        new Preceding(2),
        new Following(2)));

    windowNGramExtractors = new ArrayList<CleartkExtractor>();
    windowNGramExtractors.add(new CleartkExtractor(Token.class, wordExtractor, new Ngram(
        new Preceding(2)), new Ngram(new Following(2))));
  }

  public List<Feature> extractFeatures(JCas jCas, Token token, Sentence sentence)
      throws CleartkExtractorException {
    List<Feature> features = new ArrayList<Feature>();

    for (SimpleFeatureExtractor extractor : simpleExtractors) {
      features.addAll(extractor.extract(jCas, token));
    }

    for (CleartkExtractor extractor : windowExtractors) {
      features.addAll(extractor.extractWithin(jCas, token, sentence));
    }

    for (CleartkExtractor extractor : windowNGramExtractors) {
      features.addAll(extractor.extractWithin(jCas, token, sentence));
    }

    return features;
  }

}
