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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.transform.TrainableExtractor_ImplBase;
import org.cleartk.classifier.feature.transform.TransformableFeature;

/**
 * Scales features produced by its subextractor to have mean=0, stddev=1 for a given feature
 * <p>
 * 
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Lee Becker
 * 
 */
public class ZeroMeanUnitStddevExtractor<OUTCOME_T> extends TrainableExtractor_ImplBase<OUTCOME_T>
    implements SimpleFeatureExtractor {

  private SimpleFeatureExtractor subExtractor;

  private boolean isTrained;

  // This is read in after training for use in transformation
  private Map<String, MeanStddevTuple> meanStddevMap;

  public ZeroMeanUnitStddevExtractor(String name) {
    this(name, null);
  }

  public ZeroMeanUnitStddevExtractor(String name, SimpleFeatureExtractor subExtractor) {
    super(name);
    this.subExtractor = subExtractor;
    this.isTrained = false;
  }

  @Override
  protected Feature transform(Feature feature) {
    String featureName = feature.getName();
    MeanStddevTuple stats = this.meanStddevMap.get(featureName);
    double value = ((Number) feature.getValue()).doubleValue();
    return new Feature("ZMUS_" + featureName, (value - stats.mean) / stats.stddev);
  }

  @Override
  public List<Feature> extract(JCas view, Annotation focusAnnotation)
      throws CleartkExtractorException {

    List<Feature> extracted = this.subExtractor.extract(view, focusAnnotation);
    List<Feature> result = new ArrayList<Feature>();
    if (this.isTrained) {
      // We have trained / loaded a ZMUS model, so now fix up the values
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
    Map<String, MeanVarianceRunningStat> featureStatsMap = new HashMap<String, MeanVarianceRunningStat>();

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
              MeanVarianceRunningStat stats;
              if (featureStatsMap.containsKey(featureName)) {
                stats = featureStatsMap.get(featureName);
              } else {
                stats = new MeanVarianceRunningStat();
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

    this.meanStddevMap = new HashMap<String, MeanStddevTuple>();
    for (Map.Entry<String, MeanVarianceRunningStat> entry : featureStatsMap.entrySet()) {
      MeanVarianceRunningStat stats = entry.getValue();
      this.meanStddevMap.put(entry.getKey(), new MeanStddevTuple(stats.mean(), stats.stddev()));
    }

    this.isTrained = true;
  }

  @Override
  public void save(URI zmusDataUri) throws IOException {
    // Write out tab separated values: feature_name, mean, stddev
    File out = new File(zmusDataUri);
    BufferedWriter writer = null;
    writer = new BufferedWriter(new FileWriter(out));

    for (Map.Entry<String, MeanStddevTuple> entry : this.meanStddevMap.entrySet()) {
      MeanStddevTuple tuple = entry.getValue();
      writer.append(String.format("%s\t%f\t%f\n", entry.getKey(), tuple.mean, tuple.stddev));
    }
    writer.close();
  }

  @Override
  public void load(URI zmusDataUri) throws IOException {
    // Reads in tab separated values (feature name, mean, stddev)
    File in = new File(zmusDataUri);
    BufferedReader reader = null;
    this.meanStddevMap = new HashMap<String, MeanStddevTuple>();
    reader = new BufferedReader(new FileReader(in));
    String line = null;
    while ((line = reader.readLine()) != null) {
      String[] featureMeanStddev = line.split("\\t");
      this.meanStddevMap.put(
          featureMeanStddev[0],
          new MeanStddevTuple(
              Double.parseDouble(featureMeanStddev[1]),
              Double.parseDouble(featureMeanStddev[2])));
    }

    reader.close();

    this.isTrained = true;
  }

  private static class MeanStddevTuple {

    public MeanStddevTuple(double mean, double stddev) {
      this.mean = mean;
      this.stddev = stddev;
    }

    public double mean;

    public double stddev;
  }

  public static class MeanVarianceRunningStat implements Serializable {

    private static final long serialVersionUID = 1L;

    public MeanVarianceRunningStat() {
      this.clear();
    }

    public void init(int n, double mean, double variance) {
      this.numSamples = n;
      this.meanNew = mean;
      this.varNew = variance;
    }

    public void add(double x) {
      numSamples++;

      if (this.numSamples == 1) {
        meanOld = meanNew = x;
        varOld = 0.0;
      } else {
        meanNew = meanOld + (x - meanOld) / numSamples;
        varNew = varOld + (x - meanOld) * (x - meanNew);

        // set up for next iteration
        meanOld = meanNew;
        varOld = varNew;
      }
    }

    public void clear() {
      this.numSamples = 0;
    }

    public int getNumSamples() {
      return this.numSamples;
    }

    public double mean() {
      return (this.numSamples > 0) ? meanNew : 0.0;
    }

    public double variance() {
      return (this.numSamples > 1) ? varNew / (this.numSamples) : 0.0;
    }

    public double stddev() {
      return Math.sqrt(this.variance());
    }

    public double variancePop() {
      return (this.numSamples > 1) ? varNew / (this.numSamples - 1) : 0.0;
    }

    public double stddevPop() {
      return Math.sqrt(this.variancePop());
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
      out.defaultWriteObject();
      out.writeInt(numSamples);
      out.writeDouble(meanNew);
      out.writeDouble(varNew);

    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      in.defaultReadObject();
      numSamples = in.readInt();
      meanOld = meanNew = in.readDouble();
      varOld = varNew = in.readDouble();
    }

    private int numSamples;

    private double meanOld;

    private double meanNew;

    private double varOld;

    private double varNew;
  }
}
