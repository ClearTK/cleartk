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
import java.util.Arrays;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.CleartkExtractor;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Bag;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Covered;
import org.cleartk.classifier.feature.extractor.simple.CharacterCategoryPatternExtractor;
import org.cleartk.classifier.feature.extractor.simple.CoveredTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.FeatureExtractor1;
import org.cleartk.classifier.feature.extractor.simple.SimpleNamedFeatureExtractor;
import org.cleartk.classifier.liblinear.LIBLINEARStringOutcomeDataWriter;
import org.cleartk.timeml.type.Time;
import org.cleartk.timeml.util.CleartkInternalModelFactory;
import org.cleartk.timeml.util.TimeWordsExtractor;
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
public class TimeTypeAnnotator extends CleartkAnnotator<String> {

  public static final CleartkInternalModelFactory FACTORY = new CleartkInternalModelFactory() {
    @Override
    public Class<?> getAnnotatorClass() {
      return TimeTypeAnnotator.class;
    }

    @Override
    public Class<?> getDataWriterClass() {
      return LIBLINEARStringOutcomeDataWriter.class;
    }

    @Override
    public AnalysisEngineDescription getBaseDescription() throws ResourceInitializationException {
      return AnalysisEngineFactory.createPrimitiveDescription(TimeTypeAnnotator.class);
    }
  };

  private List<FeatureExtractor1<Time>> featuresExtractors;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    this.featuresExtractors = Lists.newArrayList();
    this.featuresExtractors.add(new LastWordExtractor<Time>());
    this.featuresExtractors.add(new CharacterCategoryPatternExtractor<Time>());
    this.featuresExtractors.add(new TimeWordsExtractor<Time>());
    this.featuresExtractors.add(new CleartkExtractor<Time, Token>(Token.class, new CoveredTextExtractor<Token>(), new Bag(new Covered())));
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    for (Time time : JCasUtil.select(jCas, Time.class)) {
      List<Feature> features = new ArrayList<Feature>();
      for (FeatureExtractor1<Time> extractor : this.featuresExtractors) {
        features.addAll(extractor.extract(jCas, time));
      }
      if (this.isTraining()) {
        this.dataWriter.write(new Instance<String>(time.getTimeType(), features));
      } else {
        time.setTimeType(this.classifier.classify(features));
      }
    }
  }

  private static class LastWordExtractor<T extends Annotation> implements SimpleNamedFeatureExtractor<T> {
    
    private String featureName;

    public LastWordExtractor() {
      this.featureName = "LastWord";
    }
    
    @Override
    public String getFeatureName() {
      return this.featureName;
    }

    @Override
    public List<Feature> extract(JCas view, T focusAnnotation) {
      String[] words = focusAnnotation.getCoveredText().split("\\W+");
      return Arrays.asList(new Feature(this.featureName, words[words.length - 1]));
    }

  }
}
