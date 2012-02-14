package org.cleartk.classifier.feature.transform;

import java.util.ArrayList;
import java.util.List;

import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;

/**
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * 
 * This implementation serves as a base for feature transformation from one feature into another
 * feature (normalizing, tf*idf, etc). Many to one transformation should extend directly from
 * {@link TrainableExtractor_ImplBase}.
 * 
 * 
 * @param instances
 *          - URI pointing to the output location for saving statistics
 */
public abstract class OneToOneTrainableExtractor_ImplBase<OUTCOME_T> implements
    TrainableExtractor<OUTCOME_T> {

  protected String name;

  public OneToOneTrainableExtractor_ImplBase(String name) {
    this.name = name;
  }

  @Override
  public Instance<OUTCOME_T> transform(Instance<OUTCOME_T> instance) {
    List<Feature> features = new ArrayList<Feature>();
    for (Feature feature : instance.getFeatures()) {
      if (this.isTransformable(feature)) {
        for (Feature origFeature : ((TransformableFeature) feature).getFeatures()) {
          features.add(this.transform(origFeature));
        }
      } else {
        features.add(feature);
      }
    }
    return new Instance<OUTCOME_T>(instance.getOutcome(), features);
  }

  protected abstract Feature transform(Feature feature);

  protected boolean isTransformable(Feature feature) {
    return feature instanceof TransformableFeature && this.name.equals(feature.getName());
  }

}
