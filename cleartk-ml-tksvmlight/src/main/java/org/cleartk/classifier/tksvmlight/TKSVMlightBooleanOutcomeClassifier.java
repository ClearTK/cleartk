/*
 * Copyright (c) 2007-2013, Regents of the University of Colorado 
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
package org.cleartk.classifier.tksvmlight;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UIMAFramework;
import org.apache.uima.util.Logger;
import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.ScoredOutcome;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;
import org.cleartk.classifier.jar.Classifier_ImplBase;
import org.cleartk.classifier.tksvmlight.model.TKSVMlightModel;

import com.google.common.annotations.Beta;

/**
 * A Tree Kernel SVM light classifier implementation. All features named with the prefix "TK"
 * treated as Tree Kernels.
 * 
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Daryl Lonnon
 * @see TKSVMlightStringOutcomeClassifier
 */
@Beta
public class TKSVMlightBooleanOutcomeClassifier extends
    Classifier_ImplBase<TreeFeatureVector, Boolean, Boolean> {

  static Logger logger = UIMAFramework.getLogger(TKSVMlightBooleanOutcomeClassifier.class);

  TKSVMlightModel model = null;

  /**
   * Constructor
   * 
   * @param featuresEncoder
   *          The features encoder used by this classifier.
   * @param outcomeEncoder
   *          The outcome encoder used by this classifier.
   * @param model
   *          The model for this classifier.
   */
  public TKSVMlightBooleanOutcomeClassifier(
      FeaturesEncoder<TreeFeatureVector> featuresEncoder,
      OutcomeEncoder<Boolean, Boolean> outcomeEncoder,
      TKSVMlightModel model) {
    super(featuresEncoder, outcomeEncoder);
    this.model = model;
  }

  /**
   * Classify a features list.
   * 
   * @param features
   *          The feature list to classify.
   * @return A Boolean of whether the features match this classification.
   */
  public Boolean classify(List<Feature> features) throws CleartkProcessingException {
    ScoredOutcome<Boolean> s = score(features);
    return s.getOutcome();
  }

  /**
   * Score a list of features against the model.
   * 
   * @param features
   *          The features to classify
   * @param maxResults
   *          The maximum number of results to return in the list (at most 2).
   * @return A list of scored outcomes ordered by likelihood.
   */
  @Override
  public List<ScoredOutcome<Boolean>> score(List<Feature> features, int maxResults)
      throws CleartkProcessingException {

    List<ScoredOutcome<Boolean>> resultList = new ArrayList<ScoredOutcome<Boolean>>();
    if (maxResults > 0) {
      resultList.add(this.score(features));
    }
    if (maxResults > 1) {
      ScoredOutcome<Boolean> v1 = resultList.get(0);
      ScoredOutcome<Boolean> v2 = new ScoredOutcome<Boolean>(!v1.getOutcome(), 1 - v1.getScore());
      resultList.add(v2);
    }
    return resultList;
  }

  private ScoredOutcome<Boolean> score(List<Feature> features) throws CleartkProcessingException {
    TreeFeatureVector featureVector = featuresEncoder.encodeAll(features);
    double prediction = model.evaluate(featureVector);
    boolean encodedResult = prediction > 0.0;
    if (encodedResult) {
      return new ScoredOutcome<Boolean>(true, prediction);
    } else {
      return new ScoredOutcome<Boolean>(false, 1 - prediction);
    }
  }
}
