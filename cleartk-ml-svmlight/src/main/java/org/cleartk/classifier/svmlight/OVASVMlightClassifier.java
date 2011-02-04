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
package org.cleartk.classifier.svmlight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.ScoredOutcome;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;
import org.cleartk.classifier.jar.Classifier_ImplBase;
import org.cleartk.classifier.sigmoid.Sigmoid;
import org.cleartk.classifier.svmlight.model.SVMlightModel;
import org.cleartk.classifier.util.featurevector.FeatureVector;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 */
public class OVASVMlightClassifier extends Classifier_ImplBase<FeatureVector, String, Integer> {

  Map<Integer, SVMlightModel> models;

  Map<Integer, Sigmoid> sigmoids;

  public OVASVMlightClassifier(
      FeaturesEncoder<FeatureVector> featuresEncoder,
      OutcomeEncoder<String, Integer> outcomeEncoder,
      Map<Integer, SVMlightModel> models,
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
  public List<ScoredOutcome<String>> score(List<Feature> features, int maxResults)
      throws CleartkProcessingException {
    FeatureVector featureVector = this.featuresEncoder.encodeAll(features);

    List<ScoredOutcome<String>> results = new ArrayList<ScoredOutcome<String>>();
    for (int i : models.keySet()) {
      double score = score(featureVector, i);
      String name = outcomeEncoder.decode(i);

      results.add(new ScoredOutcome<String>(name, score));
    }
    Collections.sort(results);

    return results.subList(0, Math.min(maxResults, results.size()));
  }

  private double score(FeatureVector fv, int i) {
    return sigmoids.get(i).evaluate(models.get(i).evaluate(fv));
  }
}
