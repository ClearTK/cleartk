/*
 * Copyright (c) 2013, Regents of the University of Colorado 
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
package org.cleartk.classifier.liblinear;

import java.util.List;
import java.util.Map;

import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;
import org.cleartk.classifier.jar.Classifier_ImplBase;

import com.google.common.collect.Maps;

import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;

/**
 * <br>
 * Copyright (c) 2013, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class GenericLIBLINEARClassifier<OUTCOME_TYPE> extends
    Classifier_ImplBase<FeatureNode[], OUTCOME_TYPE, Integer> {

  private Model model;

  public GenericLIBLINEARClassifier(
      FeaturesEncoder<FeatureNode[]> featuresEncoder,
      OutcomeEncoder<OUTCOME_TYPE, Integer> outcomeEncoder,
      Model model) {
    super(featuresEncoder, outcomeEncoder);
    this.model = model;
  }

  @Override
  public OUTCOME_TYPE classify(List<Feature> features) throws CleartkProcessingException {
    FeatureNode[] encodedFeatures = this.featuresEncoder.encodeAll(features);
    int encodedOutcome = (int)Linear.predict(this.model, encodedFeatures);
    return this.outcomeEncoder.decode(encodedOutcome);
  }

  @Override
  public Map<OUTCOME_TYPE, Double> score(List<Feature> features) throws CleartkProcessingException {
    FeatureNode[] encodedFeatures = this.featuresEncoder.encodeAll(features);
    
    // get score for each outcome
    int[] encodedOutcomes = this.model.getLabels();
    double[] scores = new double[encodedOutcomes.length];
    if (this.model.isProbabilityModel()) {
      Linear.predictProbability(this.model, encodedFeatures, scores);
    } else {
      Linear.predictValues(this.model, encodedFeatures, scores);
    }
    
    // handle 2-class model, which is special-cased by LIBLINEAR to only return one score
    if (this.model.getNrClass() == 2 && scores[1] == 0.0) {
      scores[1] = -scores[0];
    }
    
    // create scored outcome objects
    Map<OUTCOME_TYPE, Double> scoredOutcomes = Maps.newHashMap();
    for (int i = 0; i < encodedOutcomes.length; ++i) {
      OUTCOME_TYPE outcome = this.outcomeEncoder.decode(encodedOutcomes[i]);
      scoredOutcomes.put(outcome, scores[i]);
    }
    return scoredOutcomes;
  }
}
