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
package org.cleartk.examples.pos;

import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.CleartkAnnotator;
import org.cleartk.ml.Instance;
import org.cleartk.ml.feature.extractor.CleartkExtractor;
import org.cleartk.ml.feature.extractor.CoveredTextExtractor;
import org.cleartk.ml.feature.extractor.FeatureExtractor1;
import org.cleartk.ml.feature.extractor.TypePathExtractor;
import org.cleartk.ml.feature.extractor.CleartkExtractor.Following;
import org.cleartk.ml.feature.extractor.CleartkExtractor.Preceding;
import org.cleartk.ml.feature.function.CapitalTypeFeatureFunction;
import org.cleartk.ml.feature.function.CharacterNgramFeatureFunction;
import org.cleartk.ml.feature.function.FeatureFunctionExtractor;
import org.cleartk.ml.feature.function.LowerCaseFeatureFunction;
import org.cleartk.ml.feature.function.NumericTypeFeatureFunction;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.apache.uima.fit.factory.initializable.Initializable;
import org.apache.uima.fit.util.JCasUtil;

import com.google.common.collect.Lists;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Steven Bethard
 */
public class NonSequenceExamplePosAnnotator extends CleartkAnnotator<String> implements
    Initializable {

  public static final String DEFAULT_OUTPUT_DIRECTORY = "example/model";

  private List<FeatureExtractor1<Token>> tokenFeatureExtractors;

  private List<CleartkExtractor<Token, Token>> tokenSentenceFeatureExtractors;

  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    // a list of feature extractors that require only the token
    this.tokenFeatureExtractors = Lists.newArrayList();

    // a list of feature extractors that require the token and the sentence
    this.tokenSentenceFeatureExtractors = Lists.newArrayList();

    // basic feature extractors for word, stem and part-of-speech
    FeatureExtractor1<Token> wordExtractor, stemExtractor;
    wordExtractor = new CoveredTextExtractor<Token>();
    stemExtractor = new TypePathExtractor<Token>(Token.class, "stem");

    // aliases for NGram feature parameters
    CharacterNgramFeatureFunction.Orientation fromRight = CharacterNgramFeatureFunction.Orientation.RIGHT_TO_LEFT;

    // add the feature extractor for the word itself
    // also add proliferators which create new features from the word text
    this.tokenFeatureExtractors.add(new FeatureFunctionExtractor<Token>(
        wordExtractor,
        new LowerCaseFeatureFunction(),
        new CapitalTypeFeatureFunction(),
        new NumericTypeFeatureFunction(),
        new CharacterNgramFeatureFunction(fromRight, 0, 2),
        new CharacterNgramFeatureFunction(fromRight, 0, 3)));

    // add the feature extractors for the stem and part of speech
    this.tokenFeatureExtractors.add(stemExtractor);

    // add 2 stems to the left and right
    this.tokenSentenceFeatureExtractors.add(new CleartkExtractor<Token, Token>(
        Token.class,
        stemExtractor,
        new Preceding(2),
        new Following(2)));

  }

  public void process(JCas jCas) throws AnalysisEngineProcessException {
    // generate a list of training instances for each sentence in the document
    for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
      List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);

      // for each token, extract all feature values and the label
      for (Token token : tokens) {
        Instance<String> instance = new Instance<String>();

        // extract all features that require only the token annotation
        for (FeatureExtractor1<Token> extractor : this.tokenFeatureExtractors) {
          instance.addAll(extractor.extract(jCas, token));
        }

        // extract all features that require the token and sentence annotations
        for (CleartkExtractor<Token, Token> extractor : this.tokenSentenceFeatureExtractors) {
          instance.addAll(extractor.extractWithin(jCas, token, sentence));
        }

        // during training, set the outcome from the CAS and write the instance
        if (this.isTraining()) {
          instance.setOutcome(token.getPos());
          this.dataWriter.write(instance);
        }

        // during classification, set the POS from the classifier's outcome
        else {
          token.setPos(this.classifier.classify(instance.getFeatures()));
        }
      }

    }
  }

}