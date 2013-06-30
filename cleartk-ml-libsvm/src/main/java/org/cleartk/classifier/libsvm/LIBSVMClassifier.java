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
package org.cleartk.classifier.libsvm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.ScoredOutcome;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;
import org.cleartk.classifier.jar.Classifier_ImplBase;
import org.cleartk.classifier.util.featurevector.FeatureVector;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philipp Wetzler
 * 
 */
public abstract class LIBSVMClassifier<OUTCOME_TYPE, ENCODED_OUTCOME_TYPE> extends
    Classifier_ImplBase<FeatureVector, OUTCOME_TYPE, ENCODED_OUTCOME_TYPE> {

  public LIBSVMClassifier(
      FeaturesEncoder<FeatureVector> featuresEncoder,
      OutcomeEncoder<OUTCOME_TYPE, ENCODED_OUTCOME_TYPE> outcomeEncoder,
      libsvm.svm_model model) {
    super(featuresEncoder, outcomeEncoder);
    this.model = model;
  }

  public OUTCOME_TYPE classify(List<Feature> features) throws CleartkProcessingException {
    FeatureVector featureVector = this.featuresEncoder.encodeAll(features);

    ENCODED_OUTCOME_TYPE encodedOutcome = decodePrediction(libsvm.svm.svm_predict(
        this.model,
        convertToLIBSVM(featureVector)));

    return outcomeEncoder.decode(encodedOutcome);
  }

  @Override
  public List<ScoredOutcome<OUTCOME_TYPE>> score(List<Feature> features, int maxResults)
      throws CleartkProcessingException {
    FeatureVector featureVector = this.featuresEncoder.encodeAll(features);
    double[] decisionValues = new double[this.model.nr_class];
    libsvm.svm.svm_predict_probability(this.model, convertToLIBSVM(featureVector), decisionValues);
    List<ScoredOutcome<OUTCOME_TYPE>> results = Lists.newArrayList();
    for (int i = 0; i < this.model.nr_class; ++i) {
      int intLabel = this.model.label[i];
      OUTCOME_TYPE outcome = this.outcomeEncoder.decode(this.decodePrediction(intLabel));
      results.add(new ScoredOutcome<OUTCOME_TYPE>(outcome, decisionValues[i]));
    }
    Collections.sort(results, this.orderByScore);
    return results.subList(0, Math.min(results.size(), maxResults));
  }

  protected static libsvm.svm_node[] convertToLIBSVM(FeatureVector featureVector) {
    List<libsvm.svm_node> nodes = new ArrayList<libsvm.svm_node>();

    for (FeatureVector.Entry entry : featureVector) {
      libsvm.svm_node node = new libsvm.svm_node();
      node.index = entry.index;
      node.value = entry.value;
      nodes.add(node);
    }

    return nodes.toArray(new libsvm.svm_node[nodes.size()]);
  }

  protected abstract ENCODED_OUTCOME_TYPE decodePrediction(double prediction);

  protected libsvm.svm_model model;

  protected Ordering<ScoredOutcome<OUTCOME_TYPE>> orderByScore = Ordering.natural().reverse().onResultOf(
      new Function<ScoredOutcome<OUTCOME_TYPE>, Double>() {
        @Override
        public Double apply(ScoredOutcome<OUTCOME_TYPE> scoredOutcome) {
          return scoredOutcome.getScore();
        }
      });

}
