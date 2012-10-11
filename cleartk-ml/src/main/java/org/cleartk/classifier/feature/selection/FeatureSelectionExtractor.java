package org.cleartk.classifier.feature.selection;

import java.util.ArrayList;
import java.util.List;

import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.transform.TrainableExtractor_ImplBase;
import org.cleartk.classifier.feature.transform.TransformableFeature;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public abstract class FeatureSelectionExtractor<OUTCOME_T> extends
    TrainableExtractor_ImplBase<OUTCOME_T> implements Predicate<Feature> {

  public FeatureSelectionExtractor(String name) {
    super(name);
  }

  @Override
  public Instance<OUTCOME_T> transform(Instance<OUTCOME_T> instance) {
    List<Feature> features = new ArrayList<Feature>();
    for (Feature feature : instance.getFeatures()) {
      if (this.isTransformable(feature)) {
        // Filter down to selected features
        features.addAll(Collections2.filter(((TransformableFeature) feature).getFeatures(), this));
      } else {
        // Pass non-relevant features through w/o filtering
        features.add(feature);
      }
    }
    return new Instance<OUTCOME_T>(instance.getOutcome(), features);
  }

}
