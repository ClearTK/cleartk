package org.cleartk.classifier.feature.transform.extractor;

import java.util.Map;

public interface SimilarityFunction {
  public double distance(Map<String, Double> vector1, Map<String, Double> vector2);
}
