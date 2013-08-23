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
