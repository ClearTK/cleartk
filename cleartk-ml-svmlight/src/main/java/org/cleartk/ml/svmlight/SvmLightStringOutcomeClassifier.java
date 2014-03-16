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
package org.cleartk.ml.svmlight;

import java.util.List;
import java.util.Map;

import org.cleartk.ml.CleartkProcessingException;
import org.cleartk.ml.Feature;
import org.cleartk.ml.encoder.features.FeaturesEncoder;
import org.cleartk.ml.encoder.outcome.OutcomeEncoder;
import org.cleartk.ml.jar.Classifier_ImplBase;
import org.cleartk.ml.sigmoid.Sigmoid;
import org.cleartk.ml.svmlight.model.SvmLightModel;
import org.cleartk.ml.util.featurevector.FeatureVector;

import com.google.common.collect.Maps;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 */
public class SvmLightStringOutcomeClassifier extends Classifier_ImplBase<FeatureVector, String, Integer> {

  Map<Integer, SvmLightModel> models;

  Map<Integer, Sigmoid> sigmoids;

  public SvmLightStringOutcomeClassifier(
      FeaturesEncoder<FeatureVector> featuresEncoder,
      OutcomeEncoder<String, Integer> outcomeEncoder,
      Map<Integer, SvmLightModel> models,
      Map<Integer, Sigmoid> sigmoids) {
    super(featuresEncoder, outcomeEncoder);
    this.models = models;
    this.sigmoids = sigmoids;
  }

  public String classify(List<Feature> features) throws CleartkProcessingException {
    FeatureVector featureVector = this.featuresEncoder.encodeAll(features);

    int maxScoredIndex = 0;
    double maxScore = 0;
    boolean first = true;
    for (int i : models.keySet()) {
      double score = score(featureVector, i);
      if (first || score > maxScore) {
        first = false;
        maxScore = score;
        maxScoredIndex = i;
      }
    }

    return outcomeEncoder.decode(maxScoredIndex);
  }

  @Override
  public Map<String, Double> score(List<Feature> features) throws CleartkProcessingException {
    FeatureVector featureVector = this.featuresEncoder.encodeAll(features);

    Map<String, Double> results = Maps.newHashMap();
    for (int i : models.keySet()) {
      double score = score(featureVector, i);
      String name = outcomeEncoder.decode(i);

      results.put(name, score);
    }

    return results;
  }

  private double score(FeatureVector fv, int i) {
    return sigmoids.get(i).evaluate(models.get(i).evaluate(fv));
  }
}
