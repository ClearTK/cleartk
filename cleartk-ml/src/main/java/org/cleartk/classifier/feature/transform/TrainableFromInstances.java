package org.cleartk.classifier.feature.transform;

import java.net.URI;
import java.util.List;

import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;

public interface TrainableFromInstances<OUTCOME_T> {

  /**
   * In the prototypical case, train takes a collection of instances and computes statistics over
   * the values such as computing mean, standard deviation, TF*IDF, etc...
   * 
   * @param instances
   *          - URI pointing to the output location for saving statistics
   * @throws CleartkExtractorException
   */
  public void train(Iterable<Instance<OUTCOME_T>> instances) throws CleartkExtractorException;

  /**
   * Saves statistics from train in location URI
   * 
   * @param uri
   * @throws CleartkExtractorException
   */
  public void save(URI uri) throws CleartkExtractorException;

  /**
   * Loads statistics from location URI
   * 
   * @param uri
   * @throws CleartkExtractorException
   */
  public void load(URI uri) throws CleartkExtractorException;

  /**
   * Transforms the values on a collection of features
   * 
   * @param features
   * @return
   * @throws CleartkExtractorException
   */
  public List<Feature> transform(List<Feature> features) throws CleartkExtractorException;

  /**
   * Returns a list of features that are eligible for transformation
   * 
   * @param features
   * @return True if feature is eligible, False if not
   * @throws CleartkExtractorException
   */
  public List<TransformableFeature> filter(Iterable<Feature> features)
      throws CleartkExtractorException;
}
