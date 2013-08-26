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

import java.util.List;
import java.util.Map;

import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;
import org.cleartk.classifier.jar.Classifier_ImplBase;
import org.cleartk.classifier.tksvmlight.model.TreeKernelSVMModel;

import com.google.common.base.Functions;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

/**
 * <br>
 * Copyright (c) 2007-2013, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Daryl Lonnon
 * @author Tim Miller
 */
public class TreeKernelSVMStringOutcomeClassifier extends
    Classifier_ImplBase<TreeFeatureVector, String, Integer> {

  Map<Integer, TreeKernelSVMModel> models;

  /**
   * Constructor
   * 
   * @param featuresEncoder
   *          The features encoder used by this classifier.
   * @param outcomeEncoder
   *          The outcome encoder used by this classifier.
   * @param models
   *          The files for the models used by this classifier.
   */
  public TreeKernelSVMStringOutcomeClassifier(
      FeaturesEncoder<TreeFeatureVector> featuresEncoder,
      OutcomeEncoder<String, Integer> outcomeEncoder,
      Map<Integer, TreeKernelSVMModel> models) {
    super(featuresEncoder, outcomeEncoder);
    this.models = models;
  }

  /**
   * Classify a features list.
   * 
   * @param features
   *          The feature list to classify.
   * @return A String of the most likely classification.
   */
  public String classify(List<Feature> features) throws CleartkProcessingException {
    Map<String, Double> scores = this.score(features);
    Ordering<String> byScore = Ordering.natural().onResultOf(Functions.forMap(scores));
    return byScore.max(scores.keySet());
  }

  /**
   * Score a list of features against the various models used by the One Verse All SVM classifier.
   * 
   * @param features
   *          The features to classify
   * @return A map from String outcomes to likelihood.
   */
  @Override
  public Map<String, Double> score(List<Feature> features)
      throws CleartkProcessingException {
    TreeFeatureVector featureVector = this.featuresEncoder.encodeAll(features);

    Map<String, Double> results = Maps.newHashMap();
    for (int i : models.keySet()) {
      double score = predict(featureVector, i);
      String name = outcomeEncoder.decode(i);

      results.put(name, score);
    }
    return results;
  }

  private double predict(TreeFeatureVector featureVector, int i) {
    return this.models.get(i).evaluate(featureVector);
  }
 }
