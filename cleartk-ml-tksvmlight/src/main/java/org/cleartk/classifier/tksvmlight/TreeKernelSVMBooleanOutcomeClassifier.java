package org.cleartk.classifier.tksvmlight;

import java.util.List;
import java.util.Map;

import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;
import org.cleartk.classifier.jar.Classifier_ImplBase;
import org.cleartk.classifier.tksvmlight.model.TreeKernelSVMModel;

import com.google.common.collect.Maps;

public class TreeKernelSVMBooleanOutcomeClassifier 
  extends
  Classifier_ImplBase<TreeFeatureVector, Boolean, Boolean> {

  TreeKernelSVMModel model = null;

  public TreeKernelSVMBooleanOutcomeClassifier(
      FeaturesEncoder<TreeFeatureVector> featuresEncoder,
      OutcomeEncoder<Boolean, Boolean> outcomeEncoder,
      TreeKernelSVMModel model) {
    super(featuresEncoder, outcomeEncoder);
    this.model = model;
  }
  
  @Override
  public Boolean classify(List<Feature> features) throws CleartkProcessingException {
    return this.predict(features) > 0;
  }

  @Override
  public Map<Boolean, Double> score(List<Feature> features) throws CleartkProcessingException {
    double prediction = this.predict(features);
    Map<Boolean, Double> scores = Maps.newHashMap();
    scores.put(true, prediction);
    scores.put(false, 1 - prediction);
    return scores;
  }

  private double predict(List<Feature> features) throws CleartkProcessingException {
    TreeFeatureVector featureVector = featuresEncoder.encodeAll(features);
    return model.evaluate(featureVector);
  }
}
