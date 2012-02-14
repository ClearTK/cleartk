package org.cleartk.classifier.feature.transform.extractor;

import java.util.Map;

public class CosineSimilarity implements SimilarityFunction {

  @Override
  public double distance(Map<String, Double> vector1, Map<String, Double> vector2) {
    return CosineSimilarity.dotProduct(vector1, vector2)
        / (magnitude(vector1) * magnitude(vector2));
  }

  public static double dotProduct(Map<String, Double> vector1, Map<String, Double> vector2) {
    double dot = 0.0;

    if (vector1.size() > vector2.size()) {
      Map<String, Double> tmp = vector2;
      vector2 = vector1;
      vector1 = tmp;
    }

    for (Map.Entry<String, Double> entry1 : vector1.entrySet()) {
      if (vector2.containsKey(entry1.getKey())) {
        dot += entry1.getValue() * vector2.get(entry1.getKey());
      }
    }

    return dot;
  }

  public static double magnitude(Map<String, Double> vector) {
    double mag = 0.0;
    for (double v : vector.values()) {
      mag = v * v;
    }
    return Math.sqrt(mag);
  }

}
