/** 
 * Copyright (c) 2012, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
 */

package org.cleartk.classifier.feature.transform.extractor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.FeatureExtractor1;
import org.cleartk.classifier.feature.transform.OneToOneTrainableExtractor_ImplBase;
import org.cleartk.classifier.feature.transform.TransformableFeature;

/**
 * Scales features extracted by its subextractor to range 0-1, by scaling by the minimum and maximum
 * values
 * <p>
 * 
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Lee Becker
 * 
 */
public class MinMaxNormalizationExtractor<OUTCOME_T, FOCUS_T extends Annotation> extends
    OneToOneTrainableExtractor_ImplBase<OUTCOME_T> implements FeatureExtractor1<FOCUS_T> {

  private FeatureExtractor1<FOCUS_T> subExtractor;

  private boolean isTrained;

  // This is read in after training for use in transformation
  private Map<String, MinMaxPair> minMaxMap;

  public MinMaxNormalizationExtractor(String name) {
    this(name, null);
  }

  public MinMaxNormalizationExtractor(String name, FeatureExtractor1<FOCUS_T> subExtractor) {
    super(name);
    this.subExtractor = subExtractor;
    this.isTrained = false;
  }

  @Override
  protected Feature transform(Feature feature) {
    String featureName = feature.getName();
    MinMaxPair stats = this.minMaxMap.get(featureName);
    double value = ((Number) feature.getValue()).doubleValue();
    return new Feature("MINMAX_NORMED_" + featureName, (value - stats.min)
        / (stats.max - stats.min));
  }

  @Override
  public List<Feature> extract(JCas view, FOCUS_T focusAnnotation) throws CleartkExtractorException {

    List<Feature> extracted = this.subExtractor.extract(view, focusAnnotation);
    List<Feature> result = new ArrayList<Feature>();
    if (this.isTrained) {
      // We have trained / loaded a MinMax model, so now fix up the values
      for (Feature feature : extracted) {
        result.add(this.transform(feature));
      }
    } else {
      // We haven't trained this extractor yet, so just mark the existing features
      // for future modification, by creating one mega container feature
      result.add(new TransformableFeature(this.name, extracted));
    }

    return result;
  }

  @Override
  public void train(Iterable<Instance<OUTCOME_T>> instances) {
    Map<String, MinMaxRunningStat> featureStatsMap = new HashMap<String, MinMaxRunningStat>();

    // keep a running mean and standard deviation for all applicable features
    for (Instance<OUTCOME_T> instance : instances) {
      // Grab the matching zmus (zero mean, unit stddev) features from the set of all features in an
      // instance
      for (Feature feature : instance.getFeatures()) {
        if (this.isTransformable(feature)) {
          // ZMUS features contain a list of features, these are actually what get added
          // to our document frequency map
          for (Feature untransformedFeature : ((TransformableFeature) feature).getFeatures()) {
            String featureName = untransformedFeature.getName();
            Object featureValue = untransformedFeature.getValue();
            if (featureValue instanceof Number) {
              MinMaxRunningStat stats;
              if (featureStatsMap.containsKey(featureName)) {
                stats = featureStatsMap.get(featureName);
              } else {
                stats = new MinMaxRunningStat();
                featureStatsMap.put(featureName, stats);
              }
              stats.add(((Number) featureValue).doubleValue());
            } else {
              throw new IllegalArgumentException("Cannot normalize non-numeric feature values");
            }
          }
        }
      }
    }

    this.minMaxMap = new HashMap<String, MinMaxPair>();
    for (Map.Entry<String, MinMaxRunningStat> entry : featureStatsMap.entrySet()) {
      MinMaxRunningStat stats = entry.getValue();
      this.minMaxMap.put(entry.getKey(), new MinMaxPair(stats.min(), stats.max()));
    }

    this.isTrained = true;
  }

  @Override
  public void save(URI zmusDataUri) throws IOException {
    // Write out tab separated values: feature_name, mean, stddev
    File out = new File(zmusDataUri);
    BufferedWriter writer = null;
    writer = new BufferedWriter(new FileWriter(out));

    for (Map.Entry<String, MinMaxPair> entry : this.minMaxMap.entrySet()) {
      MinMaxPair pair = entry.getValue();
      writer.append(String.format(Locale.ROOT, "%s\t%f\t%f\n", entry.getKey(), pair.min, pair.max));
    }
    writer.close();
  }

  @Override
  public void load(URI zmusDataUri) throws IOException {
    // Reads in tab separated values (feature name, min, max)
    File in = new File(zmusDataUri);
    BufferedReader reader = null;
    this.minMaxMap = new HashMap<String, MinMaxPair>();
    reader = new BufferedReader(new FileReader(in));
    String line = null;
    while ((line = reader.readLine()) != null) {
      String[] featureMeanStddev = line.split("\\t");
      this.minMaxMap.put(
          featureMeanStddev[0],
          new MinMaxPair(
              Double.parseDouble(featureMeanStddev[1]),
              Double.parseDouble(featureMeanStddev[2])));
    }
    reader.close();

    this.isTrained = true;
  }

  private static class MinMaxPair {

    public MinMaxPair(double min, double max) {
      this.min = min;
      this.max = max;
    }

    public double min;

    public double max;
  }

  public static class MinMaxRunningStat implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public MinMaxRunningStat() {
      this.clear();
    }

    public void add(double x) {
      this.n++;

      if (x < min) {
        this.min = x;
      }

      if (x > max) {
        this.max = x;
      }
    }

    public void clear() {
      this.n = 0;
      this.min = Double.MAX_VALUE;
      this.max = Double.MIN_VALUE;
    }

    public int getNumSamples() {
      return this.n;
    }

    public double min() {
      return this.min;
    }

    public double max() {
      return this.max;
    }

    private double min;

    private double max;

    private int n;

  }
}
