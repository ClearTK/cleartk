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

package org.cleartk.classifier.feature.transform.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.transform.TrainableExtractor_ImplBase;
import org.cleartk.classifier.feature.transform.TransformableFeature;
import org.cleartk.classifier.feature.transform.util.MinMaxMap.MinMaxPair;

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
public class MinMaxNormalizationExtractor<OUTCOME_T> extends TrainableExtractor_ImplBase<OUTCOME_T>
    implements SimpleFeatureExtractor {

  private String key;

  private SimpleFeatureExtractor subExtractor;

  private boolean isTrained;

  // This is accumulated, totaled, and written out during training
  private Map<String, MinMaxRunningStat> featureStatsMap;

  // This is read in after training for use in transformation
  private MinMaxMap<String> minMaxMap;

  public MinMaxNormalizationExtractor(String name) {
    super(name);
    this.isTrained = false;
    this.featureStatsMap = new HashMap<String, MinMaxRunningStat>();
  }

  public MinMaxNormalizationExtractor(String name, SimpleFeatureExtractor subExtractor) {
    super(name);
    this.subExtractor = subExtractor;
    this.isTrained = false;
    this.featureStatsMap = new HashMap<String, MinMaxRunningStat>();
  }

  @Override
  public List<Feature> extract(JCas view, Annotation focusAnnotation)
      throws CleartkExtractorException {

    List<Feature> extracted = this.subExtractor.extract(view, focusAnnotation);
    List<Feature> result = new ArrayList<Feature>();
    if (this.isTrained) {
      // We have trained / loaded a MinMax model, so now fix up the values
      for (Feature feature : extracted) {
        String featureName = feature.getName();
        MinMaxPair stats = this.minMaxMap.getValues(featureName);
        double value = ((Number) feature.getValue()).doubleValue();
        result.add(new Feature("MINMAX_NORMED_" + featureName, (value - stats.min)
            / (stats.max - stats.min)));
      }
    } else {
      // We haven't trained this extractor yet, so just mark the existing features
      // for future modification, by creating one mega container feature
      result.add(new TransformableFeature(this.key, extracted));
    }

    return result;
  }

  @Override
  public void train(Iterable<Instance<OUTCOME_T>> instances) throws CleartkExtractorException {

    // keep a running mean and standard deviation for all applicable features
    for (Instance<OUTCOME_T> instance : instances) {
      // Grab the matching zmus (zero mean, unit stddev) features from the set of all features in an
      // instance
      for (TransformableFeature zmusFeature : this.filter(instance.getFeatures())) {
        // ZMUS features contain a list of features, these are actually what get added
        // to our document frequency map
        for (Feature feature : zmusFeature.getFeatures()) {
          updateFeatureStats(feature);
        }
      }
    }

    this.isTrained = true;
  }

  private void updateFeatureStats(Feature feature) throws CleartkExtractorException {
    String featureName = feature.getName();
    Object featureValue = feature.getValue();
    if (featureValue instanceof Number) {
      MinMaxRunningStat stats;
      if (this.featureStatsMap.containsKey(featureName)) {
        stats = this.featureStatsMap.get(featureName);
      } else {
        stats = new MinMaxRunningStat();
        this.featureStatsMap.put(featureName, stats);
      }
      stats.add(((Number) featureValue).doubleValue());
    } else {
      throw new CleartkExtractorException(
          "Cannot normalize non-numeric feature values",
          AnalysisEngineProcessException.ANNOTATOR_EXCEPTION,
          (Object) null);
    }
  }

  @Override
  public void save(URI zmusDataUri) throws CleartkExtractorException {
    // Write out tab separated values: feature_name, mean, stddev
    File out = new File(zmusDataUri);
    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(new FileWriter(out));

      for (Map.Entry<String, MinMaxRunningStat> entry : this.featureStatsMap.entrySet()) {
        MinMaxRunningStat stats = entry.getValue();
        writer.append(String.format("%s\t%f\t%f\n", entry.getKey(), stats.min(), stats.max()));
      }
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void load(URI zmusDataUri) throws CleartkExtractorException {
    // Reads in tab separated values (feature name, min, max)
    File in = new File(zmusDataUri);
    BufferedReader reader = null;
    this.minMaxMap = new MinMaxMap<String>();
    try {
      reader = new BufferedReader(new FileReader(in));
      String line = null;
      while ((line = reader.readLine()) != null) {
        String[] featureMeanStddev = line.split("\\t");
        this.minMaxMap.setValues(
            featureMeanStddev[0],
            Double.parseDouble(featureMeanStddev[1]),
            Double.parseDouble(featureMeanStddev[2]));
      }

      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    this.isTrained = true;
  }
}
