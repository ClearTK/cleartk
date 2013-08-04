/*
 * Copyright (c) 2011, Regents of the University of Colorado 
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
package org.cleartk.timeml.time;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkSequenceAnnotator;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instances;
import org.cleartk.classifier.chunking.BIOChunking;
import org.cleartk.classifier.feature.extractor.CharacterCategoryPatternExtractor;
import org.cleartk.classifier.feature.extractor.CleartkExtractor;
import org.cleartk.classifier.feature.extractor.CoveredTextExtractor;
import org.cleartk.classifier.feature.extractor.FeatureExtractor1;
import org.cleartk.classifier.feature.extractor.NamedFeatureExtractor1;
import org.cleartk.classifier.feature.extractor.TypePathExtractor;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Following;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Preceding;
import org.cleartk.classifier.liblinear.LIBLINEARStringOutcomeDataWriter;
import org.cleartk.timeml.type.Time;
import org.cleartk.timeml.util.CleartkInternalModelFactory;
import org.cleartk.timeml.util.TimeWordsExtractor;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

import com.google.common.collect.Lists;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class TimeAnnotator extends CleartkSequenceAnnotator<String> {

  public static final CleartkInternalModelFactory FACTORY = new CleartkInternalModelFactory() {

    @Override
    public Class<?> getAnnotatorClass() {
      return TimeAnnotator.class;
    }

    @Override
    public Class<?> getDataWriterClass() {
      return LIBLINEARStringOutcomeDataWriter.class;
    }

    @Override
    public AnalysisEngineDescription getBaseDescription() throws ResourceInitializationException {
      return AnalysisEngineFactory.createPrimitiveDescription(TimeAnnotator.class);
    }
  };

  private List<NamedFeatureExtractor1<Token>> tokenFeatureExtractors;

  private List<CleartkExtractor<Token, Token>> contextFeatureExtractors;

  private BIOChunking<Token, Time> chunking;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    // define chunking type
    this.chunking = new BIOChunking<Token, Time>(Token.class, Time.class);

    // add features: word, character pattern, stem, pos
    this.tokenFeatureExtractors = Lists.newArrayList();
    this.tokenFeatureExtractors.add(new CoveredTextExtractor<Token>());
    this.tokenFeatureExtractors.add(new CharacterCategoryPatternExtractor<Token>());
    this.tokenFeatureExtractors.add(new TimeWordsExtractor<Token>());
    this.tokenFeatureExtractors.add(new TypePathExtractor<Token>(Token.class, "stem"));
    this.tokenFeatureExtractors.add(new TypePathExtractor<Token>(Token.class, "pos"));
        
    // add window of features before and after
    this.contextFeatureExtractors = Lists.newArrayList();
    for (FeatureExtractor1<Token> extractor : this.tokenFeatureExtractors) {
      this.contextFeatureExtractors.add(new CleartkExtractor<Token, Token>(Token.class, extractor, new Preceding(
          3), new Following(3)));
    }
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {

    // classify tokens within each sentence
    for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
      List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);

      // extract features for all tokens
      List<List<Feature>> featureLists = new ArrayList<List<Feature>>();
      for (Token token : tokens) {
        List<Feature> features = new ArrayList<Feature>();
        for (FeatureExtractor1<Token> extractor : this.tokenFeatureExtractors) {
          features.addAll(extractor.extract(jCas, token));
        }
        for (CleartkExtractor<Token, Token> extractor : this.contextFeatureExtractors) {
          features.addAll(extractor.extractWithin(jCas, token, sentence));
        }
        featureLists.add(features);
      }

      // during training, convert Times to chunk labels and write the training Instances
      if (this.isTraining()) {
        List<Time> times = JCasUtil.selectCovered(jCas, Time.class, sentence);
        List<String> outcomes = this.chunking.createOutcomes(jCas, tokens, times);
        this.dataWriter.write(Instances.toInstances(outcomes, featureLists));
      }

      // during prediction, convert chunk labels to Times and add them to the CAS
      else {
        List<String> outcomes = this.classifier.classify(featureLists);
        this.chunking.createChunks(jCas, tokens, outcomes);
      }
    }

    // add IDs to all Times
    int timeIndex = 1;
    for (Time time : JCasUtil.select(jCas, Time.class)) {
      if (time.getId() == null) {
        String id = String.format("t%d", timeIndex);
        time.setId(id);
        timeIndex += 1;
      }
    }
  }
}
