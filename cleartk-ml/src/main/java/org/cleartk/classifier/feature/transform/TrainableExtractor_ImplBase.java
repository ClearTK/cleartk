package org.cleartk.classifier.feature.transform;

import java.util.ArrayList;
import java.util.List;

import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;

public abstract class TrainableExtractor_ImplBase<OUTCOME_T> implements
    TrainableExtractor<OUTCOME_T> {

  protected String name;

  public TrainableExtractor_ImplBase(String name) {
    this.name = name;
  }

  /**
   * Returns a list of features that are eligible for transformation
   * 
   * @param features
   * @return True if feature is eligible, False if not
   * @throws CleartkExtractorException
   */
  protected List<TransformableFeature> filter(List<Feature> features)
      throws CleartkExtractorException {
    ArrayList<TransformableFeature> filtered = new ArrayList<TransformableFeature>();
    for (Feature feature : features) {
      if (feature instanceof TransformableFeature && this.name.equals(feature.getName())) {
        filtered.add((TransformableFeature) feature);
      }
    }
    return filtered;
  }
}
