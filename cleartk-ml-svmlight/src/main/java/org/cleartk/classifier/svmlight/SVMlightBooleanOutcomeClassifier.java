/* 
 * Copyright (c) 2007-2011, Regents of the University of Colorado 
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
import java.util.List;

import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.ScoredOutcome;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;
import org.cleartk.classifier.sigmoid.Sigmoid;
import org.cleartk.classifier.svmlight.model.SVMlightModel;
import org.cleartk.classifier.util.featurevector.FeatureVector;

/**
 * <br>
 * Copyright (c) 2007-2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class SVMlightBooleanOutcomeClassifier extends SVMlightClassifier_ImplBase<Boolean, Boolean> {

  protected Sigmoid sigmoid;

  public SVMlightBooleanOutcomeClassifier(
      FeaturesEncoder<FeatureVector> featuresEncoder,
      OutcomeEncoder<Boolean, Boolean> outcomeEncoder,
      SVMlightModel model,
      Sigmoid sigmoid) {
    super(featuresEncoder, outcomeEncoder, model);
    this.sigmoid = sigmoid;
  }

  @Override
  protected Boolean predictionToOutcome(double prediction) {
    return this.sigmoid.evaluate(prediction) > 0.5;
  }

  @Override
  public List<ScoredOutcome<Boolean>> score(List<Feature> features, int maxResults)
      throws CleartkProcessingException {

    List<ScoredOutcome<Boolean>> resultList = new ArrayList<ScoredOutcome<Boolean>>();
    if (maxResults > 0)
      resultList.add(this.score(features));
    if (maxResults > 1) {
      ScoredOutcome<Boolean> v1 = resultList.get(0);
      ScoredOutcome<Boolean> v2 = new ScoredOutcome<Boolean>(!v1.getOutcome(), 1 - v1.getScore());
      resultList.add(v2);
    }
    return resultList;
  }

  private ScoredOutcome<Boolean> score(List<Feature> features) throws CleartkProcessingException {
    FeatureVector featureVector = featuresEncoder.encodeAll(features);

    double prediction = sigmoid.evaluate(model.evaluate(featureVector));
    boolean encodedResult = (prediction > 0.5);

    if (encodedResult) {
      return new ScoredOutcome<Boolean>(true, prediction);
    } else {
      return new ScoredOutcome<Boolean>(false, 1 - prediction);
    }
  }
}
