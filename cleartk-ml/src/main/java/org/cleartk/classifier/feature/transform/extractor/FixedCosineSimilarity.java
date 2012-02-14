package org.cleartk.classifier.feature.transform.extractor;

import java.util.Map;

/**
 * Like cosine similarity, but accepts a pre-specified vector, to avoid repeated recalculation of
 * the magnitude
 * 
 */
public class FixedCosineSimilarity implements SimilarityFunction {

  protected Map<String, Double> fixedVector;

  protected double fixedMagnitude;

  public FixedCosineSimilarity(Map<String, Double> fixedVector) {
    this.fixedVector = fixedVector;
    this.fixedMagnitude = CosineSimilarity.magnitude(fixedVector);
  }

  public double distance(Map<String, Double> vector) {
    double magnitude = CosineSimilarity.magnitude(vector);
    return (magnitude == 0.0 || fixedMagnitude == 0) ? 0.0 : CosineSimilarity.dotProduct(
        vector,
        this.fixedVector) / (magnitude * fixedMagnitude);
  }

  @Override
  public double distance(Map<String, Double> vector1, Map<String, Double> vector2) {
    return this.distance(vector1);
  }

}
