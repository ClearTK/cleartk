package org.cleartk.ml.libsvm.tk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;
import org.cleartk.classifier.jar.Classifier_ImplBase;
import org.cleartk.classifier.tksvmlight.TreeFeatureVector;
import org.cleartk.classifier.tksvmlight.model.TreeKernelSVMModel;

import com.google.common.collect.Maps;

public class TKLIBSVMBooleanOutcomeClassifier
    extends
    Classifier_ImplBase<TreeFeatureVector, Boolean, Boolean> {

  TreeKernelSVMModel model = null;
  
  public TKLIBSVMBooleanOutcomeClassifier(
      FeaturesEncoder<TreeFeatureVector> featuresEncoder,
      OutcomeEncoder<Boolean, Boolean> outcomeEncoder,
      TreeKernelSVMModel model) {
    super(featuresEncoder, outcomeEncoder);
    this.model = model;
  }

  @Override
  public Boolean classify(List<Feature> features)
      throws CleartkProcessingException {
    return this.predict(features) > 0;
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
  public Map<Boolean,Double> score(List<Feature> features)
      throws CleartkProcessingException {

    Map<Boolean,Double> results = Maps.newHashMap();
    double prediction = predict(features);
    results.put(true, prediction);
    results.put(false, 1-prediction);
    return results;
  }

  private double predict(List<Feature> features) throws CleartkProcessingException {
    TreeFeatureVector featureVector = featuresEncoder.encodeAll(features);
    return model.evaluate(featureVector);
  }    
}
