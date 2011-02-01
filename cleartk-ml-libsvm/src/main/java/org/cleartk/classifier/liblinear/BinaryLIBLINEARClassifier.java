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
package org.cleartk.classifier.liblinear;

import java.util.ArrayList;
import java.util.List;

import org.cleartk.CleartkException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.ScoredOutcome;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;
import org.cleartk.classifier.jar.Classifier_ImplBase;
import org.cleartk.classifier.liblinear.model.LIBLINEARModel;
import org.cleartk.classifier.liblinear.model.LIBLINEARModel.ScoredPrediction;
import org.cleartk.classifier.util.featurevector.FeatureVector;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philipp Wetzler
 * @author Philip Ogren
 * 
 */

public class BinaryLIBLINEARClassifier extends Classifier_ImplBase<FeatureVector, Boolean, Boolean> {

  protected LIBLINEARModel model;

  public BinaryLIBLINEARClassifier(
      FeaturesEncoder<FeatureVector> featuresEncoder,
      OutcomeEncoder<Boolean, Boolean> outcomeEncoder,
      LIBLINEARModel model) {
    super(featuresEncoder, outcomeEncoder);
    this.model = model;
  }

  public Boolean classify(List<Feature> features) throws CleartkException {
    FeatureVector featureVector = this.featuresEncoder.encodeAll(features);

    boolean encodedOutcome = (model.predict(featureVector) > 0);
    return outcomeEncoder.decode(encodedOutcome);
  }

  @Override
  public List<ScoredOutcome<Boolean>> score(List<Feature> features, int maxResults)
      throws CleartkException {
    List<ScoredOutcome<Boolean>> returnValues = new ArrayList<ScoredOutcome<Boolean>>();

    FeatureVector featureVector = this.featuresEncoder.encodeAll(features);
    List<ScoredPrediction> encodedPredictions = model.score(featureVector);
    for (ScoredPrediction prediction : encodedPredictions) {
      boolean encodedOutcome = prediction.getPrediction() > 0;
      returnValues.add(new ScoredOutcome<Boolean>(outcomeEncoder.decode(encodedOutcome), prediction
          .getScore()));
      if (maxResults == 1)
        return returnValues;
    }

    return returnValues;
  }

}
